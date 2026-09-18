# 배포 (AWS EC2)

CI가 처리하는 부분(GitHub Actions, `.github/workflows/deploy.yml`):
main에 push되면 Docker 이미지를 빌드해 GHCR(ghcr.io/glucomi/be)에 올리고, EC2에 SSH로 접속해 `docker compose pull && up -d`로 재기동한다.

아래는 **한 번만** 수동으로 해두면 되는 것들.

## 1. EC2 인스턴스 준비

1. AWS 콘솔 → EC2 → 인스턴스 시작
   - AMI: Ubuntu 26.04 LTS (Canonical)
   - 인스턴스 유형: `t3.micro` (서울 리전은 t2 계열이 없어 t3.micro가 프리티어 대상)
   - 스토리지: 16GiB (프리티어 30GiB 이내)
   - 키 페어: 새로 생성해서 `.pem` 파일 다운로드 (분실 시 재발급 불가하니 잘 보관)
2. 보안 그룹 인바운드 규칙 확인/추가 (새 보안 그룹은 기본적으로 22만 열려 있음)
   - `22` (SSH) — 내 IP만
   - `8080` (앱) — 0.0.0.0/0 (테스트용. 나중에 443/nginx로 바꾸는 걸 권장)
3. 인스턴스 접속 후 Docker 설치

```bash
chmod 400 your-key.pem
ssh -i your-key.pem ubuntu@<EC2 탄력적 IP>

sudo apt-get update -y
sudo apt-get install -y ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo $VERSION_CODENAME) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

sudo systemctl enable --now docker
sudo usermod -aG docker $USER
# 재접속해야 docker 그룹 권한이 적용됨
```

4. 배포 디렉터리 + `.env` 준비

```bash
mkdir ~/ddadang && cd ~/ddadang
vi .env   # .env.prod.example 내용을 참고해 실제 값 채워넣기
```

## 2. GitHub 저장소 설정

**Settings → Secrets and variables → Actions** 에 아래 시크릿 등록:

| 이름 | 값 |
|---|---|
| `EC2_HOST` | EC2 탄력적 IP (Elastic IP) |
| `EC2_USER` | `ubuntu` |
| `EC2_SSH_KEY` | 위에서 받은 `.pem` 파일 내용 그대로 |

`GITHUB_TOKEN`은 GHCR push/pull용으로 Actions가 자동으로 제공하므로 별도 등록 불필요.

## 3. 스왑 (OOM 방지 — 필수)

t3.micro는 RAM 908Mi에 스왑이 기본 0이라, MySQL+앱을 메모리 제한 없이 같이 띄우면 OOM으로 인스턴스가
통째로 응답 불능이 되고 `restart: unless-stopped`가 계속 재시작을 시도하면서 재부팅해도 복구가 안 되는
크래시 루프에 빠진다 (실제로 한 번 겪음). 인스턴스 준비 단계에서 반드시 스왑부터 만들어 둘 것.

```bash
sudo fallocate -l 1G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo "/swapfile none swap sw 0 0" | sudo tee -a /etc/fstab
```

`docker-compose.prod.yml`에도 이미 컨테이너별 `mem_limit`과 JVM `-Xmx256m`, MySQL
`innodb_buffer_pool_size=128M`이 반영되어 있음 — 이 값들을 건드릴 땐 908Mi(+스왑 1G) 예산을
넘지 않는지 계산할 것.

## 4. HTTPS (도메인 없이 — nginx + Let's Encrypt + sslip.io)

도메인을 따로 사지 않고도 무료로 HTTPS를 쓸 수 있다. AWS가 자동으로 붙여주는
`ec2-*.compute.amazonaws.com` 퍼블릭 DNS는 Let's Encrypt 정책상 인증서 발급이 **막혀 있어서**
사용 불가 — 대신 IP를 자동으로 도메인처럼 매핑해주는 **sslip.io**(가입 불필요)를 쓴다.

```bash
sudo apt-get install -y nginx certbot python3-certbot-nginx
```

`/etc/nginx/sites-available/ddadang` (80번에서 8080으로 프록시):

```nginx
server {
    listen 80;
    server_name <EC2 탄력적 IP를 -로 이은 값>.sslip.io;  # 예: 54-116-228-101.sslip.io

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo ln -sf /etc/nginx/sites-available/ddadang /etc/nginx/sites-enabled/ddadang
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx

sudo certbot --nginx -d <위의 sslip.io 도메인> --non-interactive --agree-tos -m <연락받을 이메일> --redirect
```

인증서는 자동 갱신 타이머가 같이 설정된다. 보안 그룹에 **80, 443** 인바운드도 열어둘 것.

발급 후 `.env`의 `ISENS_REDIRECT_URI`를 https 도메인 기준으로 바꾸고 앱만 재기동:

```bash
sed -i 's#ISENS_REDIRECT_URI=.*#ISENS_REDIRECT_URI=https://<sslip.io 도메인>/api/cgm/oauth/callback#' .env
docker compose -f docker-compose.prod.yml up -d --force-recreate app
```

i-sens 개발자센터 콘솔의 Callback URL도 이 https 주소로 등록/변경할 것.

## 5. 배포 확인

`main`에 push하면 Actions 탭에서 진행 상황 확인 가능. 완료 후:

```bash
curl https://<sslip.io 도메인>/api/cgm/readings
```
