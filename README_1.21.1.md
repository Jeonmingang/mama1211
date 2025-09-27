# UltimateServerPlugin2 — 1.21.1 / Java 21

이 폴더는 기존 `MAMA-main` 플러그인을 **Java 21 + Paper(Paper/Arclight) 1.21.1**에 맞게 빌드 설정만 업그레이드한 것입니다.
- `pom.xml`: `paper-api:1.21.1-R0.1-SNAPSHOT` 및 `maven.compiler` 21로 업데이트
- `plugin.yml`: `api-version: 1.21`
- GitHub Actions: Temurin Java 21로 빌드

> 주의: 런타임 의존(Vault, Citizens 등)은 서버에 설치되어 있어야 하며, Paper/Arclight 1.21.1 호환 버전을 사용하세요.
