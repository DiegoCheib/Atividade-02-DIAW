<div align="center">

<img src="frontend/public/android-chrome-192x192.png" width="96" alt="PUC Minas" />

# Sentinela

**Autenticação com Spring Boot + JWT e interface em React**

Atividade 02 · Desenvolvimento de Interfaces e Aplicações Web (DIAW) · PUC Minas

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-6-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-8-646CFF?style=flat-square&logo=vite&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-HS256-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)

</div>

---

## Sobre o projeto

O **Sentinela** é uma aplicação web completa de login e cadastro de usuários.

O enunciado da atividade pedia Thymeleaf para as telas. Optei por uma arquitetura desacoplada:
o backend expõe uma **API REST stateless autenticada por JWT** e o frontend é uma **SPA em React**
que consome essa API. O resultado atende aos mesmos requisitos (telas de login, cadastro e
recuperação de senha, autenticação, senhas com hash e rotas protegidas) com uma separação clara
entre servidor e interface.

Em produção o próprio Spring Boot serve o build do React, de modo que a aplicação inteira roda em
um único processo, na porta 8080.

## Telas

| Login | Cadastro |
|---|---|
| ![Tela de login](docs/login.png) | ![Tela de cadastro](docs/register.png) |

| Recuperação de senha | Área protegida |
|---|---|
| ![Recuperação de senha](docs/recoverpassword.png) | ![Painel](docs/dashboard.png) |

<div align="center">
  <img src="docs/login-mobile.png" width="260" alt="Login em tela de celular" />
  <br><em>Layout responsivo</em>
</div>

## Tecnologias

**Backend**

| Recurso | Uso |
|---|---|
| Java 17 + Spring Boot 3.5 | Base da aplicação |
| Spring Security 6 | Filtros, autorização e política de acesso |
| Spring Data JPA + Hibernate | Persistência |
| H2 (arquivo) / PostgreSQL | Banco de dados por perfil |
| JJWT 0.12 | Emissão e validação dos tokens |
| BCrypt (força 12) | Hash das senhas |
| Bean Validation | Validação dos formulários no servidor |
| Resend | Envio transacional dos links de redefinição de senha |
| JUnit 5 + MockMvc | Testes automatizados |

**Frontend**

| Recurso | Uso |
|---|---|
| React 19 + TypeScript | Interface |
| Vite 8 | Build e servidor de desenvolvimento |
| React Router 7 | Rotas e proteção de páginas |
| Tailwind CSS 4 | Estilos |
| shadcn/ui (base-ui) | Componentes de formulário |
| lucide-react | Ícones |

## Estrutura do projeto

```
Atividade-02-DIAW/
├── backend/
│   ├── src/main/java/br/pucminas/diaw/sentinela/
│   │   ├── config/         SecurityConfig, CORS, propriedades, carga inicial
│   │   ├── controller/     AuthController, DashboardController, AdminController
│   │   ├── domain/         User, RefreshToken, PasswordResetToken, Role
│   │   ├── dto/            Requests e responses (records)
│   │   ├── exception/      ApiException e tratamento global de erros
│   │   ├── repository/     Repositórios Spring Data
│   │   ├── security/       JwtService, filtro JWT, cookies, UserDetails
│   │   ├── service/        Autenticação, recuperação de senha e envio pelo Resend
│   │   └── validation/     @StrongPassword e @PasswordConfirmation
│   ├── src/main/resources/
│   │   ├── application.yml         Perfil padrão (H2 em arquivo)
│   │   └── application-prod.yml    Perfil de produção (PostgreSQL)
│   └── src/test/java/...           Testes unitários e de integração
│
├── frontend/
│   ├── public/             Favicons e logo da PUC Minas
│   └── src/
│       ├── components/     Formulários, layout, guardas de rota, ui/ (shadcn)
│       ├── context/        AuthProvider
│       ├── hooks/          useAuth
│       ├── lib/            Cliente HTTP com renovação automática de token
│       ├── pages/          login, register, recuperação/redefinição, dashboard, 404
│       └── index.css       Tema e identidade visual
│
└── docs/                   Imagens usadas neste README
```

## Como executar

### Pré-requisitos

- **JDK 17 ou superior** (`java -version`)
- **Node.js 20 ou superior** (`node -v`)
- Maven não é necessário: o projeto inclui o Maven Wrapper (`./mvnw`)

### Modo desenvolvimento (dois terminais)

```bash
# Terminal 1 — API em http://localhost:8080
cd backend
./mvnw spring-boot:run

# Terminal 2 — interface em http://localhost:5173
cd frontend
npm install
npm run dev
```

Acesse **http://localhost:5173/login**.

O Vite encaminha as chamadas `/api` para a porta 8080, então não há problema de CORS durante o
desenvolvimento e o cookie de sessão continua funcionando.

### Modo produção (aplicação única)

```bash
# 1. Gera o build do React dentro dos recursos estáticos do Spring
cd frontend && npm install && npm run build

# 2. Sobe a aplicação completa
cd ../backend && ./mvnw spring-boot:run
```

Acesse **http://localhost:8080/login**. O Spring Boot serve as páginas do React e a API no mesmo
processo.

Para gerar o JAR executável com tudo dentro:

```bash
cd backend
./mvnw -Pfullstack clean package     # compila o React e empacota junto
java -jar target/sentinela-1.0.0.jar
```

### Testes

```bash
cd backend
./mvnw test
```

São 28 testes: política de senha, emissão e validação de JWT e os fluxos completos de autenticação
e redefinição de senha com MockMvc (token de uso único, troca de senha e revogação das sessões).

## Contas de demonstração

Criadas automaticamente no primeiro start, para facilitar a avaliação.

| E-mail | Usuário | Senha | Perfil |
|---|---|---|---|
| `admin@pucminas.br` | `admin` | `Admin@1234` | ADMIN |
| `demo@pucminas.br` | `demo` | `Demo@1234` | USER |

Desative com `DEMO_USER=false` (ou use o perfil `prod`, onde já vêm desligadas).

## Endpoints

### Exigidos pela atividade

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/login` | Exibe a tela de login |
| GET | `/register` | Exibe a tela de cadastro |
| POST | `/register` | Processa os dados do cadastro |
| GET | `/recoverpassword` | Exibe a tela de recuperação de senha |
| POST | `/recoverpassword` | Processa a solicitação de recuperação |

As rotas `GET` são atendidas pelo React Router. Quando o build está empacotado no backend, o
Spring encaminha esses caminhos para o `index.html` (veja `WebConfig`), então os endpoints
respondem tanto no servidor de desenvolvimento quanto na aplicação final.

Os `POST` acima existem como atalhos e delegam para os endpoints canônicos da API, que são os
efetivamente consumidos pelo React.

### API REST

| Método | Endpoint | Autenticação | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | pública | Cadastra um usuário. Responde `201` com os dados públicos |
| POST | `/api/auth/login` | pública | Valida credenciais, devolve o access token e grava o cookie de refresh |
| POST | `/api/auth/refresh` | cookie | Rotaciona o refresh token e emite um novo access token |
| POST | `/api/auth/logout` | cookie | Revoga a sessão e limpa o cookie |
| GET | `/api/auth/me` | Bearer | Dados do usuário autenticado |
| POST | `/api/auth/recoverpassword` | pública | Gera o token e envia o link de redefinição |
| GET | `/api/auth/resetpassword?token=...` | pública | Verifica se o link ainda é válido |
| POST | `/api/auth/resetpassword` | pública | Valida o token e grava a nova senha |
| GET | `/api/dashboard` | Bearer | Resumo da área protegida |
| GET | `/api/admin/users` | Bearer + ADMIN | Lista todos os usuários |
| GET | `/api/health` | pública | Verificação de disponibilidade |

Exemplos:

```bash
# Cadastro
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Maria Silva","username":"maria","email":"maria@pucminas.br",
       "password":"Senha@2026","confirmPassword":"Senha@2026"}'

# Login (guarda o cookie de refresh em cookies.txt)
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"identifier":"maria@pucminas.br","password":"Senha@2026","rememberMe":false}'

# Área protegida
curl http://localhost:8080/api/dashboard -H "Authorization: Bearer <access_token>"
```

Erros seguem sempre o mesmo formato, o que permite ao formulário destacar o campo exato:

```json
{
  "timestamp": "2026-09-09T14:28:24.903Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Verifique os dados informados",
  "path": "/api/auth/register",
  "fieldErrors": [
    { "field": "password", "message": "A senha deve ter no minimo 8 caracteres, com letra maiuscula, minuscula, numero e simbolo" }
  ]
}
```

## Como a autenticação funciona

```
Navegador                          Spring Boot
   │                                    │
   │  POST /api/auth/login              │
   ├───────────────────────────────────►│  valida com BCrypt
   │                                    │
   │  access token (JSON, 15 min)       │  refresh token (cookie HttpOnly, 7 ou 30 dias)
   │◄───────────────────────────────────┤
   │                                    │
   │  GET /api/dashboard                │
   │  Authorization: Bearer <token>     │
   ├───────────────────────────────────►│  JwtAuthenticationFilter valida a assinatura
   │                                    │  e recarrega o usuário do banco
   │                                    │
   │  token expirou → 401               │
   │  POST /api/auth/refresh (cookie)   │
   ├───────────────────────────────────►│  revoga o token antigo e emite um novo par
   │◄───────────────────────────────────┤
```

Decisões de segurança adotadas:

- **Senhas com BCrypt de força 12.** Nenhuma senha trafega ou é gravada em texto puro.
- **Access token curto (15 minutos)** guardado apenas em memória no React. Não usamos
  `localStorage`, o que reduz o impacto de um eventual XSS.
- **Refresh token opaco em cookie `HttpOnly`**, inacessível ao JavaScript. No banco fica apenas o
  hash SHA-256 do token, com caminho restrito a `/api/auth` e `SameSite`.
- **Rotação de refresh token.** Cada renovação invalida o token anterior. Se um token já usado
  reaparece, todas as sessões do usuário são revogadas.
- **Bloqueio temporário** de 15 minutos após 5 tentativas de login malsucedidas, por combinação de
  identificador e IP.
- **Mensagem genérica no login.** A resposta não revela se o erro foi no usuário ou na senha.
- **Recuperação de senha sem enumeração de contas.** A resposta é a mesma exista ou não o e-mail.
- **Autorização por perfil** com `@PreAuthorize`, validada por testes.
- **CORS restrito** às origens configuradas, e cabeçalhos de segurança incluindo CSP.

## Validações

Aplicadas no React para resposta imediata e novamente no servidor, que é a fonte de verdade.

| Campo | Regra |
|---|---|
| Nome | Obrigatório, de 3 a 120 caracteres |
| Usuário | De 3 a 40 caracteres, apenas letras, números, ponto, hífen e underline. Único |
| E-mail | Formato válido, até 180 caracteres. Único |
| Senha | Mínimo de 8 caracteres, com maiúscula, minúscula, número e símbolo |
| Confirmação | Precisa ser idêntica à senha |

O cadastro exibe um medidor de força que espelha exatamente a regra do backend. Usuário e e-mail
duplicados retornam `409` apontando o campo em conflito.

## Configuração e credenciais

Nenhum segredo está no código. Tudo vem de variáveis de ambiente, com valores padrão apenas para
desenvolvimento local. O arquivo `backend/.env.example` documenta as variáveis; copie para `.env`
ou exporte no shell.

| Variável | Padrão | Descrição |
|---|---|---|
| `JWT_SECRET` | *(gerada por execução)* | Chave HMAC de assinatura. **Mínimo de 32 caracteres.** Sem ela, uma chave aleatória é gerada no start e as sessões caem a cada reinício |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:4173` | Origens liberadas, separadas por vírgula |
| `COOKIE_SECURE` | `false` | Use `true` em produção, para enviar o cookie só por HTTPS |
| `DEMO_USER` | `true` | Criação das contas de demonstração |
| `SERVER_PORT` | `8080` | Porta da aplicação |
| `RESEND_API_KEY` | — | Chave `re_...` para enviar e-mails; sem ela, o link aparece apenas no log |
| `MAIL_FROM` | `Sentinela <onboarding@resend.dev>` | Remetente cadastrado no Resend |
| `MAIL_REPLY_TO` | — | Endereço opcional para respostas |
| `APP_BASE_URL` | `http://localhost:5173` | Origem pública usada no link do e-mail |
| `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` | — | Apenas no perfil `prod`, com PostgreSQL |

Gere uma chave adequada com:

```bash
openssl rand -base64 48
```

O `.gitignore` cobre `.env`, `backend/data/` (banco H2), `target/`, `node_modules/` e o build do
React, então nenhuma credencial ou artefato de build vai para o repositório.

Executando em produção:

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
export DATABASE_URL="jdbc:postgresql://localhost:5432/sentinela"
export DB_USERNAME=sentinela DB_PASSWORD=troque-esta-senha
export CORS_ORIGINS="https://seu-dominio.com.br"
java -jar target/sentinela-1.0.0.jar --spring.profiles.active=prod
```

## Banco de dados

No perfil padrão o H2 grava em `backend/data/sentinela.mv.db`, então os cadastros sobrevivem ao
reinício. O schema é criado pelo Hibernate.

| Tabela | Conteúdo |
|---|---|
| `users` | Dados da conta, hash da senha, perfil, datas de criação e último acesso |
| `refresh_tokens` | Hash do token, validade, revogação, IP e user agent da sessão |
| `password_reset_tokens` | Hash do link de redefinição, validade, uso e IP solicitante |

Para inspecionar o banco, suba com `H2_CONSOLE=true` e acesse `/h2-console` usando a URL
`jdbc:h2:file:./data/sentinela`.

## Recuperação de senha

A tela `/recoverpassword` responde sempre com a mesma mensagem para não revelar quais e-mails
existem na base. Para contas válidas, o backend cria um token aleatório, persiste somente seu hash
SHA-256 e envia pelo Resend um link para `/resetpassword`. O link expira em 30 minutos, só pode ser
usado uma vez e um novo pedido invalida os anteriores. Ao trocar a senha, todas as sessões abertas
do usuário são revogadas.

Para habilitar o envio local, crie `backend/.env` a partir de `.env.example` e preencha
`RESEND_API_KEY`. Esse arquivo é carregado automaticamente quando o backend é iniciado a partir da
pasta `backend`. O remetente padrão `onboarding@resend.dev` serve para testes da conta Resend; para
enviar a qualquer destinatário, configure em `MAIL_FROM` um endereço de domínio verificado.

Sem a chave, o fluxo continua testável: o conteúdo do e-mail e o link são exibidos no log do
backend. Falhas do provedor não revelam se a conta existe e não alteram a resposta pública.

## Atendimento ao enunciado

| Requisito | Situação |
|---|---|
| Tela de login em `/login` com usuário/e-mail, senha, botão, links de cadastro e recuperação | Atendido |
| Mensagens de erro para credenciais inválidas | Atendido |
| Tela de registro em `/register` com nome, e-mail, senha e confirmação | Atendido |
| Validações de campos vazios, e-mail inválido, senhas divergentes e duplicidade | Atendido |
| Autenticação, identificação do usuário e bloqueio de acesso não autenticado | Atendido, via JWT e Spring Security |
| Armazenamento seguro de senhas | Atendido, BCrypt de força 12 |
| Encerramento de sessão | Atendido, com revogação do refresh token |
| Endpoints obrigatórios | Atendidos |
| Interface própria e responsiva | Atendido |
| Credenciais fora do código-fonte | Atendido, por variáveis de ambiente |
| Recuperação de senha por e-mail (opcional) | Atendido, com Resend e token de uso único |
| Thymeleaf | Substituído por React, conforme combinado |

## Autor

- Diego Cheib

PUC Minas · Desenvolvimento de Interfaces e Aplicações Web · 2026
