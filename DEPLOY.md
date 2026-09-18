# 배포 (AWS EC2)

CI가 처리하는 부분(GitHub Actions, `.github/workflows/deploy.yml`):
main에 push되면 Docker 이미지를 빌드해 GHCR(ghcr.io/glucomi/be)에 올리고, EC2에 SSH로 접속해 `docker compose pull && up -d`로 재기동한다.

아래는 **한 번만** 수동으로 해두면 되는 것들.

## 1. EC2 인스턴스 준비

1. AWS 콘솔 → EC2 → 인스턴스 시작
   - AMI: Amazon Linux 2023 (또는 Ubuntu 22.04)
   - 인스턴스 유형: `t2.micro` (프리티어)
   - 키 페어: 새로 생성해서 `.pem` 파일 다운로드 (분실 시 재발급 불가하니 잘 보관)
2. 보안 그룹 인바운드 규칙 추가
   - `22` (SSH) — 내 IP만
   - `8080` (앱) — 0.0.0.0/0 (테스트용. 나중에 443/nginx로 바꾸는 걸 권장)
3. 인스턴스 접속 후 Docker 설치

```bash
ssh -i your-key.pem ec2-user@<EC2 퍼블릭 IP>

sudo yum update -y
sudo yum install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
# 재접속해야 docker 그룹 권한이 적용됨

# docker compose plugin
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
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
| `EC2_HOST` | EC2 퍼블릭 IP 또는 도메인 |
| `EC2_USER` | `ec2-user` (Amazon Linux) 또는 `ubuntu` (Ubuntu) |
| `EC2_SSH_KEY` | 위에서 받은 `.pem` 파일 내용 그대로 |

`GITHUB_TOKEN`은 GHCR push/pull용으로 Actions가 자동으로 제공하므로 별도 등록 불필요.

## 3. i-sens 개발자센터에 redirect_uri 등록

EC2 IP/도메인 기준으로 콜백 URL을 등록해야 실제로 연동이 동작한다.

```
http://<EC2 퍼블릭 IP>:8080/api/cgm/oauth/callback
```

`.env`의 `ISENS_REDIRECT_URI`도 동일한 값으로 맞출 것.

## 4. 배포 확인

`main`에 push하면 Actions 탭에서 진행 상황 확인 가능. 완료 후:

```bash
curl http://<EC2 퍼블릭 IP>:8080/api/cgm/readings
```
