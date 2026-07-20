<div align="center">



# <img alt="youlai-boot" width="28" valign="middle" src="./docs/images/logo/logo.png"> youlai-boot-tenant



**Multi-tenant enterprise-grade permission management backend based on Spring Boot**



[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?logo=spring-boot)](https://spring.io/projects/spring-boot)

[![JDK](https://img.shields.io/badge/JDK-17%2B-007396?logo=openjdk)](https://openjdk.org/)

[![Multi-tenancy](https://img.shields.io/badge/Multi--tenancy-SaaS-blue)](#)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue?logo=apache)](LICENSE)

[![Gitee Star](https://gitee.com/youlaiorg/youlai-boot-tenant/badge/star.svg)](https://gitee.com/youlaiorg/youlai-boot-tenant/stargazers)



</div>



![](https://foruda.gitee.com/images/1708618984641188532/a7cca095_716974.png "rainbow.png")



<div align="center">



[🖥️ Live Preview](https://vue.youlai.tech) | [📱 Mobile Preview](https://app.youlai.tech) | [📖 Documentation](https://www.youlai.tech/docs/server/spring-boot/)



</div>



## Introduction



**youlai-boot-tenant** is the multi-tenant edition of [youlai-boot](https://gitee.com/youlaiorg/youlai-boot), built on Spring Boot. It adopts a single-database multi-tenant approach with MyBatis-Plus, isolating tenant data by tenant ID, and is designed as the backend for SaaS applications. It ships with the frontend [vue3-element-admin](https://gitee.com/youlaiorg/vue3-element-admin) and shares the same API specification.



## Core Features



- 🏢 **Multi-tenant architecture** — single-database multi-tenancy with MyBatis-Plus, data isolation by tenant ID

- 🔐 **Security** — Spring Security + JWT/Redis dual-session model, token renewal, multi-device mutual exclusion

- 🛡️ **Fine-grained permissions** — 5-level RBAC: data → menu → button → API → field

- ⚡ **Code generator** — one-click generation of full-stack CRUD code

- 📦 **Complete modules** — users, roles, menus, departments, dictionaries, files, scheduled tasks, message center, operation logs

- 🔌 **Real-time communication** — SSE push: online user count, dictionary sync, notification broadcast



## System Preview



**PC**



<table align="center">

  <tr>

    <td><img alt="PC Preview 1" width="400" src="./docs/images/preview/pc-01.png"></td>

    <td><img alt="PC Preview 2" width="400" src="./docs/images/preview/pc-02.png"></td>

  </tr>

  <tr>

    <td><img alt="PC Preview 3" width="400" src="./docs/images/preview/pc-03.png"></td>

    <td><img alt="PC Preview 4" width="400" src="./docs/images/preview/pc-04.png"></td>

  </tr>

  <tr>

    <td><img alt="PC Preview 5" width="400" src="./docs/images/preview/pc-05.png"></td>

    <td><img alt="PC Preview 6" width="400" src="./docs/images/preview/pc-06.png"></td>

  </tr>

</table>



**Mobile**



<table align="center">

  <tr>

    <td><img alt="App Preview 1" width="200" src="./docs/images/preview/app-01.png"></td>

    <td><img alt="App Preview 2" width="200" src="./docs/images/preview/app-02.png"></td>

    <td><img alt="App Preview 3" width="200" src="./docs/images/preview/app-03.png"></td>

    <td><img alt="App Preview 4" width="200" src="./docs/images/preview/app-04.png"></td>

  </tr>

</table>



## Quick Start



**Requirements**: JDK 17+ · MySQL 8.0+ · Redis 6.0+



1. Clone: `git clone https://gitee.com/youlaiorg/youlai-boot-tenant.git`

2. Import database: `sql/youlai_admin_tenant.sql`

3. Adjust config (optional, a read-only online data source is configured by default): `src/main/resources/application-dev.yml`

4. Start and visit http://localhost:8000/doc.html



Default credentials: `admin` / `123456`



**Docker**: `cd docker && docker-compose up -d`



Detailed guide: [Deployment Docs](https://www.youlai.tech/docs/server/spring-boot/deploy) · [Dev Standards](https://www.youlai.tech/docs/server/spring-boot/dev-standards)



## Multi-Tenant Testing



- **Prebuilt tenants**: the default platform tenant (`tenant_id=0`) and a demo tenant (`tenant_id=1`).

- **Prebuilt accounts**: platform tenant (`root`/`admin`) and demo tenant (`admin`), default password `123456`.

- **Local testing**: edit your local `hosts` file to add `127.0.0.1 vue.youlai.tech` and `127.0.0.1 demo.youlai.tech`; visiting via different domains switches tenants automatically.



## Tech Stack



| Tech | Version | Description |

|:-----|:--------|:------------|

| Spring Boot | 4.0.5 | Core framework |

| Spring Security | 6.x | Auth & authorization |

| MyBatis-Plus | 3.5.15 | ORM |

| Druid | 1.2.24 | Connection pool |

| Redis + Redisson | 6.0+ / 4.1.0 | Cache · Session · Distributed lock |

| Caffeine | 2.9.3 | Local cache |

| XXL-Job | 3.2.0 | Distributed scheduled tasks |

| Knife4j | 4.5.0 | API docs |

| MapStruct | 1.6.3 | Object mapping |

| MinIO | 8.5.10 | Object storage |



## Directory Structure



```

youlai-boot-tenant/

├── docker/                          # Docker orchestration

├── sql/                             # Database init scripts

├── src/main/java/com/youlai/boot/

│   ├── YouLaiBootApplication.java   # Bootstrap class

│   ├── auth/                        # Auth (login/logout/token)

│   ├── codegen/                     # Code generator

│   ├── common/                      # Common module (constants/enums/response)

│   ├── file/                        # File service (MinIO/local/OSS)

│   ├── framework/                   # Technical framework layer

│   │   ├── apidoc/                  # OpenAPI / Knife4j

│   │   ├── cache/                   # Redis / Caffeine cache

│   │   ├── captcha/                 # Graphic captcha

│   │   ├── integration/             # SMS / Email / WeChat

│   │   ├── job/                     # XXL-Job scheduled tasks

│   │   ├── mybatis/                 # MyBatis-Plus config

│   │   ├── security/                # Security / JWT / Token

│   │   └── web/                     # Global exception / CORS / rate limit

│   ├── message/                     # SSE push

│   └── system/                      # Business (user/role/menu/dept)

└── pom.xml                          # Maven dependency management

```



## Ecosystem



**Frontend**



| Project | Stack | Description |

|:-----|:------|:------------|

| [vue3-element-admin](https://gitee.com/youlaiorg/vue3-element-admin) | Vue 3 + Element Plus | PC admin frontend (recommended) |

| [youlai-app](https://gitee.com/youlaiorg/youlai-app) | Vue 3 + UniApp | Mobile App |



**Backend**



| Project | Stack | Description |
| [youlai-boot](https://gitee.com/youlaiorg/youlai-boot) | Spring Boot + MyBatis-Plus | Java (recommended) |
| [youlai-nest](https://gitee.com/youlaiorg/youlai-nest) | NestJS + TypeORM | Node.js |
| [youlai-gin](https://gitee.com/youlaiorg/youlai-gin) | Go + Gorm | Go |
| [youlai-django](https://gitee.com/youlaiorg/youlai-django) | Django + DRF | Python |
| [youlai-fastapi](https://gitee.com/youlaiorg/youlai-fastapi) | FastAPI + SQLAlchemy | Python |
| [youlai-think](https://gitee.com/youlaiorg/youlai-think) | ThinkPHP + ThinkORM | PHP |
| [youlai-aspnet](https://gitee.com/youlaiorg/youlai-aspnet) | ASP.NET Core + EF Core | C# |
| [youlai-axum](https://gitee.com/youlaiorg/youlai-axum) | Axum + SeaORM | Rust |
> **youlai-boot** also provides the following variants and branches: [Multi-Tenant](https://gitee.com/youlaiorg/youlai-boot-tenant) · [MyBatis-Flex](https://gitee.com/youlaiorg/youlai-boot-flex) · [Spring Boot 3](https://gitee.com/youlaiorg/youlai-boot/tree/spring-boot-3) · [PostgreSQL](https://gitee.com/youlaiorg/youlai-boot/tree/db-pg) · [Multi-Module](https://gitee.com/youlaiorg/youlai-boot/tree/multi-module)

>

> The eight backends share the same **RESTful API specification** and **database schema**, so the frontend can switch seamlessly.



## Documentation



| Resource | Link |

|:-----|:-----|

| 📖 Full docs site | [www.youlai.tech](https://www.youlai.tech/) |

| 🖥️ PC live preview | [vue.youlai.tech](https://vue.youlai.tech) |

| 📱 Mobile live preview | [app.youlai.tech](https://app.youlai.tech) |

| 🔗 Apifox API docs | [apifox.com](https://www.apifox.cn/apidoc/shared-195e783f-4d85-4235-a038-eec696de4ea5) |

| 🔗 Local API docs | [localhost:8000/doc.html](http://localhost:8000/doc.html) |



## Contributing



Issues and Pull Requests are welcome! See the [Contribution Guide](https://www.youlai.tech/faq/help).



[![Contributors](https://contrib.rocks/image?repo=haoxianrui/youlai-boot)](https://github.com/haoxianrui/youlai-boot/graphs/contributors)



## License



Released under the [Apache License 2.0](LICENSE); free for commercial use.



---



<p align="center">

  <img src="./docs/images/qrcode/wechat-official.jpg" height="180" alt="Official WeChat Account">

  &nbsp;&nbsp;&nbsp;&nbsp;

  <img src="./docs/images/qrcode/wechat-mp.jpg" height="180" alt="Mini Program">

  &nbsp;&nbsp;&nbsp;&nbsp;

  <img src="./docs/images/qrcode/wechat-personal.png" height="180" alt="Add author on WeChat">

</p>

<p align="center">

  <sub>Official WeChat Account</sub>

  &nbsp;&nbsp;&nbsp;&nbsp;

  <sub>Mini Program</sub>

  &nbsp;&nbsp;&nbsp;&nbsp;

  <sub>Add author on WeChat</sub>

</p>

<p align="center"><em>Technical discussion · Feedback · Business cooperation</em></p>

