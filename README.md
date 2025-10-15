# hemograma-analises
# Sistema de Monitoramento de Hemogramas

Sistema de software para processamento automático de hemogramas, identificação de sinais relevantes e notificação de gestores de saúde pública em tempo real.

## Visão Geral


## Arquitetura


## EQUIPE

| Integrante | Matrícula Institucional |
|:---:|:---:|
| CARLOS ANTONIO | 202009548 |
| THIAGO VICENTE DE AQUINO | 202201716 |
| --- | --- |


## Status do Projeto

### Marco 1 - Recepção FHIR (Concluído)

### Marco 2 - Análise Individual (Em desenvolvimento)

### Marco 3 - Base Consolidada (Planejado)

### Marco 4 - Análise Coletiva (Planejado)

## CRONOGRAMA

| SEMANAS | ATIVIDADE |
|:---:|:---|
|SEMANA 1|CONFIGURAÇÃO INICIAL DO PROJETO, CONFIGURAR O RECEPTOR DE JSON FHIR, TESTAR O SOBSCRIPTION DO HAPI FHIR|
|SEMANA 2|IMPLEMENTAÇÃO DAS LOGICAS DE NEGOCIO DO PROGRAMA|
|SEMANA 3|PERSISTENCIA DOS DADOS E DOS ALERTAS NO SGBD. INICIO DA IMPLEMENTAÇÃO DO FRONTEND (FAZER AS NOTIFICAÇÕES)|
|SEMANA 4|LOGICA DA JANELA DESLIZANTE. FINALIZAR A INTERNACE DO APLICATIVO. FILANIZAÇÃO DA DOCUMENTAÇÃO E SLIDES |

## TECNOLOGIAS E ARQUITETURA

O projeto será desenvolvido seguindo uma arquitetura em **Camadas** (Cliente-Servidor), utilizando tecnologias que garantem a interoperabilidade com o padrão HL7 FHIR.

### 1. Camada de Apresentação e Alertas Ubíquos

Esta camada é responsável pela interface do usuário e pela entrega de informações em tempo real (conceito ubíquo).

| Componente | Tecnologia | Propósito no Projeto |
| :--- | :--- | :--- |
| **Aplicativo Móvel (Frontend)** | **React Native com Expo**| Interface do App Android para consulta de alertas e visualização de hemogramas. |
| **Notificações Ubíquas** | **Firebase Cloud Messaging (FCM)** | Serviço para o envio de **notificações push em tempo real** do Backend para o App (Requisito de Ubiquidade). |

### 2. Camada de Aplicação e Domínio (Backend Core)

Responsável pela lógica de negócios, análise de dados e comunicação com o padrão de saúde.

| Componente | Tecnologia | Propósito no Projeto |
| :--- | :--- | :--- |
| **Linguagem / Framework** | **Java** com **Spring Boot** | Oferece um ambiente para a criação da API REST e a lógica de análise de desvios (Marcos 2 e 4). |
| **Padrão de Interoperabilidade** | **HAPI FHIR** | Biblioteca para o **parsing** e manipulação dos recursos de saúde do padrão FHIR (Marco 1). |
| **Comunicação** | **API REST** (Cliente-Servidor) | Endpoints para o App Móvel consultar alertas e hemogramas. |

### 3. Camada de Dados (Persistência)

Responsável pelo armazenamento dos dados de hemogramas recebidos.

| Componente | Tecnologia | Propósito no Projeto |
| :--- | :--- | :--- |
| **SGBD (Não Relacional)** | **MongoDB Atlas** | Solução de banco de dados em nuvem. Ideal para armazenar o JSON do FHIR diretamente, acelerando o desenvolvimento da **Base Consolidada (Marco 3)**. |
| **Framework de Persistência** | **Spring Data MongoDB** | Facilita a integração do Spring Boot com o MongoDB. |

---

### Ferramentas de Processo e Qualidade

| Área | Ferramentas | Foco no Projeto |
| :--- | :--- | :--- |
| **Controle de Versão** | **Git / GitHub** | Registro de atividades, colaboração e comprovação de participação. |
| **Gestão de Projeto** | **GitHub Projects** ou **Trello** | Organização do Backlog, Sprints e clara divisão de responsabilidades. |
| **Testes** | **JUnit 5 / Mockito** | Cobertura de testes automatizados e garantia de qualidade do código (5% da nota). |
