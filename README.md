# 💰 API Inteligente de Orçamento com Spring AI

Projeto desenvolvido como parte do **Desafio de Projeto de AI Java Back-End da DIO**, utilizando **Spring Boot**, **Spring AI** e Inteligência Artificial para criar uma API capaz de registrar e consultar transações financeiras através de comandos em texto e áudio.

O projeto foi baseado na implementação apresentada na trilha de Spring Boot da DIO e posteriormente evoluído para utilizar **Google Gemini** como modelo de linguagem e **Groq + Whisper** para reconhecimento de fala.

---

## Projeto base

Este projeto foi desenvolvido a partir do conteúdo oficial da trilha de Spring Boot da **Digital Innovation One (DIO)**.

Repositório utilizado como base:

https://github.com/digitalinnovationone/dio-spring-boot-learning-track

Módulo de Spring AI utilizado como referência:

https://github.com/digitalinnovationone/dio-spring-boot-learning-track/tree/main/05-spring-ai

A implementação presente neste repositório parte dessa base e foi modificada para utilizar **Google Gemini** no processamento de linguagem natural e **Groq + Whisper** para reconhecimento de fala.


---

## Objetivo

O objetivo deste projeto é demonstrar como integrar recursos de Inteligência Artificial a uma aplicação Java real, mantendo uma arquitetura organizada e separando as responsabilidades da aplicação.

A API permite que o usuário envie comandos em linguagem natural, como:

> "Gastei 35 reais na farmácia."

A Inteligência Artificial interpreta o comando e utiliza **Tool Calling** para executar uma função real da aplicação, como cadastrar ou consultar uma transação financeira.

Também é possível enviar um arquivo de áudio contendo o comando.

Por exemplo:

> 🎤 "Gastei 42 reais no mercado."

O áudio é transcrito automaticamente e o texto resultante é processado pela IA.

---

#  Arquitetura

## Fluxo utilizando texto

```text
Comando em texto
       ↓
Spring Boot
       ↓
Spring AI / ChatClient
       ↓
Google Gemini
       ↓
Tool Calling
       ↓
Casos de uso da aplicação
       ↓
Spring Data JPA
       ↓
MySQL
```

## Fluxo utilizando áudio

```text
Arquivo de áudio
       ↓
Spring Boot
       ↓
Groq API + Whisper
       ↓
Transcrição para texto
       ↓
Spring AI / ChatClient
       ↓
Google Gemini
       ↓
Tool Calling
       ↓
Casos de uso da aplicação
       ↓
Spring Data JPA
       ↓
MySQL
```

Dessa forma, a Inteligência Artificial não apenas responde perguntas, mas consegue utilizar funções reais disponibilizadas pela aplicação.

---

# Funcionalidades

- Cadastro manual de transações através de API REST;
- Consulta de transações por categoria;
- Processamento de comandos financeiros em linguagem natural;
- Cadastro de transações utilizando comandos de texto;
- Consulta de informações utilizando comandos de texto;
- Reconhecimento de fala através de arquivos de áudio;
- Transcrição de áudio em português;
- Integração com Google Gemini;
- Integração com Groq API;
- Utilização do Whisper para Speech-to-Text;
- Tool Calling com Spring AI;
- Persistência das transações no MySQL;
- Validação básica dos arquivos de áudio;
- Execução do banco através de Docker Compose;
- API REST para acesso às funcionalidades.

---

# Recursos implementados

O projeto original apresentado durante a trilha utiliza serviços da **OpenAI** para os recursos de Inteligência Artificial e áudio.

Nesta implementação, o projeto foi adaptado para utilizar outros provedores.

### Google Gemini

Responsável por:

- interpretação dos comandos em linguagem natural;
- utilização do `ChatClient`;
- decisão de qual operação executar;
- Tool Calling.

### Groq + Whisper

Responsável por:

- recebimento dos arquivos de áudio;
- reconhecimento de fala;
- transcrição de áudio para texto.

O fluxo original:

```text
OpenAI
  ↓
Chat + Speech-to-Text
```

foi adaptado para:

```text
Groq / Whisper
      ↓
Transcrição
      ↓
Gemini
      ↓
Tool Calling
```

Essa alteração permite experimentar o fluxo da aplicação utilizando serviços que possuem opções gratuitas para desenvolvimento, evitando a necessidade de utilizar uma API paga da OpenAI durante os testes.

Também foi criado um endpoint separado para comandos em texto, permitindo testar o funcionamento da IA sem a necessidade de enviar um arquivo de áudio.

---

# Tecnologias utilizadas

| Tecnologia | Utilização |
|-|-|
| Java 25 | Linguagem principal |
| Spring Boot 4 | Backend e API REST |
| Spring AI | Integração da aplicação com modelos de IA |
| Google Gemini | Interpretação de linguagem natural e Tool Calling |
| Groq API | Serviço utilizado para processamento do áudio |
| Whisper | Speech-to-Text / transcrição |
| Spring Data JPA | Persistência |
| MySQL | Banco de dados |
| Docker | Containers |
| Gradle | Build e gerenciamento de dependências |
| REST | Comunicação com a aplicação |

---

# 📂 Estrutura geral

A estrutura principal do projeto é semelhante a:

```text
desafio-spring-ai-dio/
│
├── gradle/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── dio/
│   │   │       └── budgeting/
│   │   │
│   │   └── resources/
│   │       ├── prompts/
│   │       └── application.properties
│   │
│   └── test/
│
├── build.gradle
├── compose.yml
├── gradlew
├── gradlew.bat
├── README.md
└── settings.gradle
```

Os comandos Gradle e Docker apresentados neste README devem ser executados na **pasta raiz do projeto**, ou seja, na pasta onde estão `build.gradle`, `gradlew` e `compose.yml`.

---

# Endpoints

## Criar uma transação manualmente

```http
POST /transactions
```

Content-Type:

```text
application/json
```

Exemplo:

```json
{
  "description": "Compra no supermercado",
  "amount": 120.50,
  "category": "GROCERIES"
}
```

---

## Consultar transações por categoria

```http
GET /transactions/{category}
```

Exemplo:

```http
GET /transactions/GROCERIES
```

Algumas categorias utilizadas pela aplicação:

```text
GROCERIES
PHARMA
AUTO
```

---

## Comando de IA por texto

```http
POST /transactions/ai
```

Content-Type:

```text
text/plain
```

Exemplo:

```text
Gastei 35 reais na farmácia
```

O Gemini interpreta a mensagem e pode utilizar as ferramentas disponibilizadas pela aplicação para executar a operação necessária.

---

## Comando de IA por áudio

```http
POST /transactions/ai/audio
```

Content-Type:

```text
multipart/form-data
```

O arquivo de áudio é enviado para a Groq API e transcrito utilizando Whisper.

Depois:

```text
Áudio
 ↓
Whisper
 ↓
Texto
 ↓
Gemini
 ↓
Tool Calling
 ↓
Aplicação
```

---

# Como executar o projeto

## Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- Git;
- Java 25;
- Docker Desktop ou Docker Engine;
- Docker Compose;
- uma chave da Gemini API;
- uma chave da Groq API.

> O projeto utiliza o **Gradle Wrapper**, portanto não é necessário instalar o Gradle manualmente.

---

# Verificando o Java

## Windows

Abra o PowerShell:

```powershell
java -version
```

## macOS / Linux

```bash
java -version
```

O projeto foi desenvolvido utilizando Java 25.

---

# Verificando o Docker

Execute:

```bash
docker --version
```

Depois:

```bash
docker compose version
```

Se ambos retornarem suas respectivas versões, o Docker está disponível.

No Windows/macOS utilizando Docker Desktop, certifique-se também de que o Docker Desktop esteja iniciado.

---

# Como obter as chaves das APIs

O projeto utiliza dois serviços externos:

| Serviço | Função |
|---|---|
| Google Gemini | IA e Tool Calling |
| Groq + Whisper | Reconhecimento de fala |

As chaves devem ser armazenadas em variáveis de ambiente.

**Nunca coloque as API Keys diretamente no código-fonte ou em um repositório público.**

---

## Criando uma chave do Google Gemini

Acesse:

https://aistudio.google.com/

Depois:

1. Entre utilizando uma conta Google;
2. Procure pela opção de criação/gerenciamento de **API Keys**;
3. Crie uma nova chave;
4. Copie a chave;
5. Guarde-a em um local seguro.

A aplicação espera encontrar a chave através da variável:

```text
GEMINI_API_KEY
```

> A disponibilidade dos modelos e os limites gratuitos da API podem mudar. Consulte as informações atuais do Google AI Studio.

---

## Criando uma chave da Groq

Acesse:

https://console.groq.com/

Depois:

1. Entre ou crie uma conta;
2. Abra a seção **API Keys**;
3. Clique para criar uma nova chave;
4. Copie a chave;
5. Guarde-a em um local seguro.

A aplicação espera encontrar:

```text
GROQ_API_KEY
```

Neste projeto, a Groq é utilizada para acessar o modelo Whisper responsável pela transcrição dos arquivos de áudio.

---

# Clonando o projeto

Primeiro escolha onde deseja armazenar o projeto.

## Windows

Por exemplo, para utilizar a Área de Trabalho:

```powershell
cd C:\Users\SEU_USUARIO\Desktop
```

Clone:

```powershell
git clone URL_DO_REPOSITORIO
```

Entre na pasta:

```powershell
cd desafio-spring-ai-dio
```

Confirme:

```powershell
dir
```

Você deverá encontrar arquivos como:

```text
build.gradle
compose.yml
gradlew
gradlew.bat
README.md
settings.gradle
```

---

## macOS

Por exemplo:

```bash
cd ~/Desktop
```

Clone:

```bash
git clone URL_DO_REPOSITORIO
```

Entre:

```bash
cd desafio-spring-ai-dio
```

Confira:

```bash
ls
```

---

## Linux

Escolha uma pasta:

```bash
cd ~
```

Clone:

```bash
git clone URL_DO_REPOSITORIO
```

Entre:

```bash
cd desafio-spring-ai-dio
```

Confira:

```bash
ls
```

---

# Configurando as API Keys

## Windows

Abra o PowerShell e execute:

```powershell
setx GEMINI_API_KEY "SUA_CHAVE_GEMINI"
```

Depois:

```powershell
setx GROQ_API_KEY "SUA_CHAVE_GROQ"
```

Após utilizar `setx`, **feche o PowerShell e abra um novo terminal**.

Isso é necessário para que as novas variáveis sejam carregadas.

Você pode verificar se elas existem sem revelar as chaves:

```powershell
if ($env:GEMINI_API_KEY) { "Gemini configurado" }
```

```powershell
if ($env:GROQ_API_KEY) { "Groq configurado" }
```

Depois volte para a pasta do projeto:

```powershell
cd C:\caminho\para\desafio-spring-ai-dio
```

---

# Configurando no macOS

O macOS normalmente utiliza o shell Zsh.

Execute:

```bash
export GEMINI_API_KEY="SUA_CHAVE_GEMINI"
export GROQ_API_KEY="SUA_CHAVE_GROQ"
```

Essas variáveis existirão somente durante a sessão atual.

Para torná-las persistentes, abra:

```bash
nano ~/.zshrc
```

Adicione:

```bash
export GEMINI_API_KEY="SUA_CHAVE_GEMINI"
export GROQ_API_KEY="SUA_CHAVE_GROQ"
```

Salve e execute:

```bash
source ~/.zshrc
```

Verifique:

```bash
[ -n "$GEMINI_API_KEY" ] && echo "Gemini configurado"
[ -n "$GROQ_API_KEY" ] && echo "Groq configurado"
```

---

# Configurando no Linux

Se estiver utilizando Bash:

```bash
export GEMINI_API_KEY="SUA_CHAVE_GEMINI"
export GROQ_API_KEY="SUA_CHAVE_GROQ"
```

Para torná-las persistentes:

```bash
nano ~/.bashrc
```

Adicione:

```bash
export GEMINI_API_KEY="SUA_CHAVE_GEMINI"
export GROQ_API_KEY="SUA_CHAVE_GROQ"
```

Depois:

```bash
source ~/.bashrc
```

Verifique:

```bash
[ -n "$GEMINI_API_KEY" ] && echo "Gemini configurado"
[ -n "$GROQ_API_KEY" ] && echo "Groq configurado"
```

---

# Iniciando o MySQL

Certifique-se de estar **dentro da pasta do projeto**.

## Windows

```powershell
cd C:\caminho\para\desafio-spring-ai-dio
```

## macOS / Linux

```bash
cd /caminho/para/desafio-spring-ai-dio
```

Agora execute:

```bash
docker compose up -d
```

O Docker criará/iniciará o MySQL utilizado pela aplicação.

Verifique:

```bash
docker ps
```

---

# 🔨 Compilando o projeto

Certifique-se novamente de estar na pasta que contém:

```text
build.gradle
gradlew
gradlew.bat
```

## Windows

```powershell
.\gradlew.bat clean build
```

## macOS / Linux

Na primeira execução pode ser necessário dar permissão ao Gradle Wrapper:

```bash
chmod +x gradlew
```

Depois:

```bash
./gradlew clean build
```

O resultado esperado é:

```text
BUILD SUCCESSFUL
```

---

# ▶️ Executando a aplicação

## Windows

Na raiz do projeto:

```powershell
.\gradlew.bat bootRun
```

## macOS / Linux

```bash
./gradlew bootRun
```

Espere aparecer uma mensagem semelhante a:

```text
Started BudgetingApplication
```

A API estará sendo executada em:

```text
http://localhost:8080
```

> O terminal que está executando `bootRun` deve permanecer aberto.

Abra **um segundo terminal** para executar os testes abaixo.

---

# Testando a aplicação

## 1. Consultar transações

### Windows

```powershell
Invoke-RestMethod http://localhost:8080/transactions/GROCERIES
```

### macOS / Linux

```bash
curl http://localhost:8080/transactions/GROCERIES
```

---

# Criando uma transação manualmente

## Windows

```powershell
$body = @{
    description = "Compra no supermercado"
    amount = 120.50
    category = "GROCERIES"
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri http://localhost:8080/transactions `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

## macOS / Linux

```bash
curl -X POST \
  http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Compra no supermercado",
    "amount": 120.50,
    "category": "GROCERIES"
  }'
```

Depois consulte:

```bash
curl http://localhost:8080/transactions/GROCERIES
```

---

# Testando a IA por texto

Experimente o comando:

```text
Gastei 35 reais na farmácia
```

## Windows

```powershell
Invoke-RestMethod `
    -Uri http://localhost:8080/transactions/ai `
    -Method Post `
    -ContentType "text/plain; charset=utf-8" `
    -Body "Gastei 35 reais na farmacia"
```

## macOS / Linux

```bash
curl -X POST \
  http://localhost:8080/transactions/ai \
  -H "Content-Type: text/plain; charset=utf-8" \
  --data "Gastei 35 reais na farmacia"
```

O fluxo executado será:

```text
"Gastei 35 reais na farmácia"
              ↓
         Spring Boot
              ↓
          Spring AI
              ↓
            Gemini
              ↓
         Tool Calling
              ↓
    PersistTransactionUseCase
              ↓
             JPA
              ↓
            MySQL
```

Para verificar a transação:

## Windows

```powershell
Invoke-RestMethod http://localhost:8080/transactions/PHARMA
```

## macOS / Linux

```bash
curl http://localhost:8080/transactions/PHARMA
```

---

# Consultando utilizando IA

Também é possível utilizar linguagem natural para solicitar informações.

Por exemplo:

```text
Liste meus gastos de supermercado
```

## Windows

```powershell
Invoke-RestMethod `
    -Uri http://localhost:8080/transactions/ai `
    -Method Post `
    -ContentType "text/plain; charset=utf-8" `
    -Body "Liste meus gastos de supermercado"
```

## macOS / Linux

```bash
curl -X POST \
  http://localhost:8080/transactions/ai \
  -H "Content-Type: text/plain; charset=utf-8" \
  --data "Liste meus gastos de supermercado"
```

Nesse caso, o Gemini pode utilizar o Tool Calling para executar o caso de uso responsável pela consulta das transações.

---

# Testando o reconhecimento de fala

Grave um arquivo de áudio dizendo, por exemplo:

> "Gastei 42 reais no mercado."

Salve o arquivo como:

```text
teste.m4a
```

Também podem ser utilizados outros formatos suportados pelo serviço de transcrição.

---

## Windows

Supondo que o arquivo esteja na Área de Trabalho:

```powershell
C:\Windows\System32\curl.exe `
  -X POST `
  -F "file=@C:\Users\SEU_USUARIO\Desktop\teste.m4a" `
  http://localhost:8080/transactions/ai/audio
```

---

## macOS

Se estiver na Área de Trabalho:

```bash
curl -X POST \
  -F "file=@$HOME/Desktop/teste.m4a" \
  http://localhost:8080/transactions/ai/audio
```

---

## Linux

Por exemplo:

```bash
curl -X POST \
  -F "file=@$HOME/teste.m4a" \
  http://localhost:8080/transactions/ai/audio
```

---

## Fluxo do reconhecimento de fala

```text
🎤 Arquivo de áudio
        ↓
Spring Boot recebe MultipartFile
        ↓
Groq API
        ↓
Whisper Large V3 Turbo
        ↓
"Gastei 42 reais no mercado"
        ↓
Spring AI / ChatClient
        ↓
Gemini
        ↓
Tool Calling
        ↓
PersistTransactionUseCase
        ↓
JPA
        ↓
MySQL
        ↓
✅ Transação registrada
```

Depois consulte a categoria:

## Windows

```powershell
Invoke-RestMethod http://localhost:8080/transactions/GROCERIES
```

## macOS / Linux

```bash
curl http://localhost:8080/transactions/GROCERIES
```

---

# Validação de áudio

O endpoint de áudio realiza uma validação básica antes de enviar o arquivo para o serviço de transcrição.

Caso nenhum arquivo seja enviado ou o arquivo esteja vazio, a API retorna uma resposta de erro em vez de tentar processar o áudio.

Isso evita chamadas desnecessárias ao serviço externo.

---

# Segurança das chaves

As chaves não ficam armazenadas diretamente no projeto.

O `application.properties` utiliza variáveis de ambiente.

Exemplo:

```properties
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
groq.api-key=${GROQ_API_KEY}
```

Portanto:

```text
Código
  ↓
Variável de ambiente
  ↓
API Key
```

e não:

```text
Código
  ↓
API Key escrita diretamente ❌
```

### Nunca faça:

```properties
groq.api-key=gsk_MINHA_CHAVE_REAL
```

ou:

```properties
spring.ai.google.genai.api-key=MINHA_CHAVE_REAL
```

principalmente antes de enviar o projeto para um repositório público.

---

# Encerrando a aplicação

No terminal onde `bootRun` está executando:

```text
Ctrl + C
```

Para parar os containers:

```bash
docker compose down
```

Os dados armazenados no volume Docker podem permanecer disponíveis para a próxima execução, dependendo da configuração do `compose.yml`.

---

# Resumo rápido da execução

## Windows

Abra o PowerShell e entre no projeto:

```powershell
cd C:\caminho\para\desafio-spring-ai-dio
```

Suba o banco:

```powershell
docker compose up -d
```

Compile:

```powershell
.\gradlew.bat clean build
```

Execute:

```powershell
.\gradlew.bat bootRun
```

Depois abra outro PowerShell para testar a API.

---

## macOS

```bash
cd ~/caminho/para/desafio-spring-ai-dio

docker compose up -d

chmod +x gradlew

./gradlew clean build

./gradlew bootRun
```

Abra outro terminal para executar os testes.

---

## Linux

```bash
cd ~/caminho/para/desafio-spring-ai-dio

docker compose up -d

chmod +x gradlew

./gradlew clean build

./gradlew bootRun
```

Abra outro terminal para executar os testes.

---

# Build

Para validar o projeto completo:

## Windows

```powershell
.\gradlew.bat clean build
```

## macOS / Linux

```bash
./gradlew clean build
```

Resultado esperado:

```text
BUILD SUCCESSFUL
```

---

# O que aprendi:

Durante o desenvolvimento deste projeto foi possível praticar:

- desenvolvimento de APIs REST com Spring Boot;
- arquitetura em camadas;
- criação de controllers;
- criação e utilização de casos de uso;
- persistência utilizando Spring Data JPA;
- utilização do Hibernate;
- integração com MySQL;
- execução de banco de dados utilizando Docker;
- Docker Compose;
- Gradle e Gradle Wrapper;
- Spring AI;
- utilização do `ChatClient`;
- integração com modelos de linguagem;
- Google Gemini;
- Tool Calling;
- integração com APIs externas;
- reconhecimento de fala;
- Whisper;
- Groq API;
- envio e processamento de arquivos `multipart/form-data`;
- utilização de `MultipartFile`;
- utilização de `RestClient`;
- variáveis de ambiente;
- proteção de API Keys;
- validação básica de requisições;
- testes de endpoints utilizando PowerShell;
- testes utilizando cURL;
- adaptação de uma aplicação para diferentes provedores de Inteligência Artificial.

Um dos principais aprendizados foi perceber que a camada de Inteligência Artificial pode ser integrada às regras reais da aplicação.

Neste projeto, o modelo não apenas gera uma resposta textual. Através de **Tool Calling**, ele consegue decidir quando executar funções Java responsáveis por cadastrar ou consultar informações no banco de dados.

---

# Melhorias futuras

O projeto pode continuar evoluindo.

Algumas possibilidades:

### 🖥️ Interface gráfica

Criar uma interface web para que o usuário não precise utilizar PowerShell ou cURL.

Uma possível interface poderia possuir:

- dashboard financeiro;
- formulário de transações;
- histórico de gastos;
- filtros por categoria;
- campo para comandos de IA.

### 🎤 Gravação pelo navegador

Adicionar um botão:

```text
🎤 Gravar comando
```

permitindo falar diretamente pelo navegador.

O fluxo poderia ser:

```text
Microfone
   ↓
Navegador
   ↓
Spring Boot
   ↓
Groq / Whisper
   ↓
Gemini
   ↓
Tool Calling
```

### 📊 Dashboard

Exibir:

- total gasto;
- gastos por categoria;
- últimas transações;
- gráficos;
- resumo financeiro.

### Mais testes automatizados

Adicionar testes específicos para:

- controllers;
- casos de uso;
- Tool Calling;
- tratamento de erros;
- serviço de transcrição.

### Swagger / OpenAPI

Adicionar documentação interativa dos endpoints.

### 🔐 Autenticação

Adicionar usuários e autenticação utilizando Spring Security.

### 🔊 Text-to-Speech

Gerar uma resposta em áudio para o usuário.

---

# Sobre o desafio

Este projeto foi desenvolvido como parte do **Desafio Final do Bootcamp de AI Java Back-End da Digital Innovation One (DIO)**.

O objetivo do desafio é evoluir uma API inteligente capaz de:

- receber comandos;
- utilizar Inteligência Artificial;
- executar funções reais da aplicação;
- trabalhar com transações financeiras;
- utilizar recursos de áudio;
- persistir informações.

Além da execução do projeto-base, esta versão foi adaptada para utilizar **Google Gemini e Groq/Whisper**, explorando a possibilidade de utilizar diferentes provedores de IA dentro de uma aplicação Spring.

---
# Referências

- Digital Innovation One — Spring Boot Learning Track  
  https://github.com/digitalinnovationone/dio-spring-boot-learning-track

- Projeto final de Spring AI da trilha  
  https://github.com/digitalinnovationone/dio-spring-boot-learning-track/tree/main/05-spring-ai

- Google AI Studio / Gemini API  
  https://aistudio.google.com/

- GroqCloud  
  https://console.groq.com/


Projeto desenvolvido para fins de estudo e prática durante o desafio da **Digital Innovation One (DIO)**.

---

# 📄 Licença

Projeto desenvolvido para fins educacionais.