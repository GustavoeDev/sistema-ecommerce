<div id="top" align="center">

# Sistema-Ecommerce - Monorepo

[![Java 21 - Requerido](https://img.shields.io/badge/Java-21--requerido-blue?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Docker Compose disponível](https://img.shields.io/badge/Docker%20Compose-dispon%C3%ADvel-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)

**Status do projeto:** MVP funcional para `orders-service`; restante dos microsserviços em planejamento.

Conjunto de microsserviços para um sistema de e-commerce de alta demanda — este repositório centraliza serviços relacionados ao fluxo de vendas, inventário e processamento de pedidos.

</div>

---

## Sumário

- [Visão Geral do Sistema](#visao-geral-do-sistema)
- [Microsserviços](#microsservicos)
- [Funcionalidades Principais](#funcionalidades-principais)
- [Arquitetura e Fluxos](#arquitetura-e-fluxos)
- [Como Executar](#como-executar-quickstart)
  - [Executando localmente o `orders-service`](#executando-localmente-o-orders-service)
  - [Executando com Docker Compose (banco + aplicação)](#executando-com-docker-compose-banco-aplicacao)
- [Configuração / Variáveis de ambiente](#executando-com-docker-banco-aplicacao)
- [Testes](#testes)

---

## Visão Geral do Sistema

Este projeto tem como objetivo construir um sistema de e-commerce completo e resiliente, preparado para cenários de alta demanda (por exemplo Black Friday). O foco é garantir integridade do estoque, evitar perda de vendas e permitir escalabilidade horizontal por meio de microsserviços.

As responsabilidades principais incluem:

- Gestão de catálogo de produtos.
- Fluxo de compra completo.
- Processamento assíncrono de pedidos via `orders-processing-service`.
- Promoções e cupons.
- Cálculo de frete baseado em CEP e características do pedido.
- Recomendações simples de produtos e avaliações dos clientes.

---

## Microsserviços

- `orders-service` (Em andamento) — Fornece APIs REST para gerenciar produtos, categorias, usuários e pedidos; persiste dados com Spring Data JPA.

- `orders-processing-service` (A implementar) — Consome eventos do `orders-service` via mensageria (RabbitMQ). Orquestrador principal dos serviços de (`payments-service`, `shipping-service`).

- `payments-service` — Simulação/integração de pagamento.

- `shipping-service` — Serviço de envio de encomenda.

Cada serviço ficará isolado e terá seu próprio ciclo de build e deploy.

---

## Funcionalidades Principais

- Gerenciamento de produtos e categorias.
- Controle de estoque direto no `orders-service`, evitando vendas excedentes.
- Criação e validação de cupons e promoções (regras: validade, uso por cliente, restrições por produto/categoria).
- Cálculo de frete por CEP e dimensões/peso do pedido.
- Fluxo de compra com criação de pedido e simulação de pagamento.
- Processamento assíncrono de pedidos (via `orders-processing-service`).
- Avaliações de produto e cálculo de nota média.
- Recomendações simples baseadas em histórico de compras.

---

## Arquitetura e Fluxos

Visão de alto nível:

1. Cliente (front-end) consome APIs REST do `orders-service`.
2. Ao criar um pedido:
   - `orders-service` valida dados e **checa estoque diretamente no banco** (`stock_quantity`).
   - Cria o pedido no banco.
   - Publica evento na fila RabbitMQ para processamento assíncrono.
3. `orders-processing-service` consome eventos:
   - Chamará os serviços `payments-service` e `shipping-service` para processamento do pedido.
   - Aplica política de retry ou marca pedido para intervenção manual em caso de falha.

---

## Documentação da API

Documentação automática OpenAPI (via Springdoc) para cada microsserviço:

- OpenAPI / Swagger UI: `http://{host}:{port}/swagger-ui/index.html` ou `/swagger-ui.html`

---

## Exemplos de requisições / respostas

Abaixo exemplos minimalistas para testar rapidamente as APIs.

1) Listar produtos

Request:

```http
GET /api/products HTTP/1.1
Host: localhost:8080
Accept: application/json
```

Response (200)

```json
[
  {
    "id": "4f9b3c4e-7e12-4d2b-9c33-2c9b6a7e91ab",
    "name": "Camiseta Básica",
    "description": "Camiseta 100% algodão, confortável e leve",
    "price": 39.90,
    "stockQuantity": 120,
    "weight": 0.25,
    "averageRating": 4.7,
    "active": true,
    "category": {
      "id": "12fa8a3c-0e76-4d1d-abcd-551c88e1f562",
      "name": "Roupas",
      "description": "Categoria de roupas e vestuário"
    }
  }
]
```

2) Criar um pedido

Request:

```http
POST /api/orders HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "items": [
    { 
      "productId": "11111111-1111-1111-1111-111111111111", 
      "quantity": 2 
    },
    { 
      "productId": "55555555-5555-5555-5555-555555555555", 
      "quantity": 1 
    }
  ],
  "addressId": "99999999-9999-9999-9999-999999999999",
  "couponCode": "BLACKFRIDAY10"
}
```

Response (201)

```json
{
  "id": "7fb88c6a-3f8a-42c4-8a35-6db4bd8b52fa",
  "createdAt": "2025-12-01T21:05:33",
  "status": "CREATED",
  "client": {
    "id": "2f8e3e1b-228f-4e59-9f32-82df5ee08fb4",
    "name": "Gustavo Emanuel",
    "email": "gustavo@example.com"
  },
  "address": {
    "id": "99999999-9999-9999-9999-999999999999",
    "street": "Rua Exemplo",
    "number": "123",
    "city": "São Paulo",
    "state": "SP",
    "postalCode": "01001-000"
  },
  "coupon": {
    "id": "c92c0020-3f9c-41b5-9c1f-75978128e5bf",
    "code": "BLACKFRIDAY10",
    "discountPercentage": 10
  },
  "items": [
    {
      "productId": "11111111-1111-1111-1111-111111111111",
      "name": "Camiseta Básica",
      "quantity": 2,
      "unitPrice": 39.90,
      "totalPrice": 79.80
    },
    {
      "productId": "55555555-5555-5555-5555-555555555555",
      "name": "Boné Preto",
      "quantity": 1,
      "unitPrice": 29.90,
      "totalPrice": 29.90
    }
  ],
  "totalAmount": 109.70,
  "shippingCost": 15.00,
  "discountAmount": 10.97
}

```

3) Exemplo de resposta de erro padrão

```json
{
  "timestamp": "2025-11-30T12:34:56.789",
  "status": 404,
  "error": "Not Found",
  "message": "Produto não encontrado: id=999"
}
```

---

### Executando localmente o `orders-service`

1. Navegue até a pasta do serviço:

```sh
cd orders-service
```

2. Build (usando o Maven Wrapper):

```sh
./mvnw clean package
```

3. Rodar em modo desenvolvimento via Maven:

```sh
./mvnw spring-boot:run
```

4. Ou executar o JAR gerado:

```sh
java -jar target/orders-service-0.0.1-SNAPSHOT.jar
```

A aplicação utiliza por padrão as variáveis definidas em application.yaml, mas você pode sobrescrevê-las usando variáveis de ambiente.

Se não houver server.port, o Spring Boot inicia na porta 8080.

<a id="executando-com-docker-compose-banco-aplicacao"></a>
### Executando com Docker Compose (banco + aplicação)

O diretório orders-service contém um docker-compose.yml que inicia:

- Postgres (postgres:15)
- orders-service (sua aplicação Spring Boot)

Para subir tudo:

```sh
docker compose up -d --build
```

O Compose já fornece as variáveis de ambiente necessárias para a aplicação e para o banco.

<a id="configuracao-variaveis-de-ambiente"></a>
### Arquivo .env (opcional)

Se quiser sobrescrever variáveis externas ou rodar localmente, crie um .env com:

```ini
DB_URL=jdbc:postgresql://localhost:5432/orders_db
DB_USER=orders_user
DB_PASSWORD=OrdersP4ssw0rd
```

### Variáveis de ambiente disponíveis


DB_URL — URL JDBC do Postgres:
```ini
ex: jdbc:postgresql://localhost:5432/orders_db
```

DB_USER — usuário do banco:
```ini
ex: orders_user
```

DB_PASSWORD — senha do banco:
```ini
ex: OrdersP4ssw0rd
```

---

## Testes

O módulo `orders-service` usa JUnit 5, Spring Boot Test e Mockito. Para executar os testes do serviço:

```sh
cd orders-service
./mvnw test
```

