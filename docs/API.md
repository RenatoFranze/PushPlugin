# Referência da API

- [.init()](#pushinitoptions)
- [push.on()](#pushonevent-callback)
  - [push.on('registration')](#pushonregistration-callback)
  - [push.on('notification')](#pushonnotification-callback)
  - [push.on('error')](#pushonerror-callback)
- [push.off()](#pushoffevent-callback)
- [push.unregister()](#pushunregistersuccesshandler-errorhandler)

## Push.init(options)

Inicializa o plugin.

**Nota:** é necessário esperar o `deviceready` para inicializar o plugin.

### Retorno

- Instância de `Push`.

### Parâmetros

Parâmetro | Tipo | Padrão | Descrição
----------|------|---------|----------
`options` | `Object` | `{}` | Objeto descrevendo opções específicas da plataforma abaixo.

#### Android

Atributo | Tipo | Padrão | Descrição
---------|------|---------|----------
`android.senderID` | `string` | | Número do projeto no GCM.
`android.sound` | `boolean` | `true` | Opcional. Se `true`, ativa o som de notificação.
`android.vibrate` | `boolean` | `true` | Opcional. Se `true`, o dispositivo vibra quando receber a notificação.

### Exemplo

```javascript
var push = Push.init({
    android: {
        senderID: "1234567890",
        sound: true
    }
});
```

## push.on(event, callback)

### Parametros

Parâmetro | Tipo | Padrão | Descrição
----------|------|---------|----------
`event` | `string` | | Nome do evento para ser escutado. Abaixo segue a lista dos eventos.
`callback` | `Function` | | Função chamada quando o evento é acionado.

## push.on('registration', callback)

O evento `registration` será acionado a cada registro feito com sucesso no GCM.

### Parâmetros do callback

Parâmetro | Tipo | Descrição
----------|------|---------
`data.registrationId` | `string` | Contêm o ID de registro do GCM.

### Exemplo

```javascript
push.on('registration', function(data) {
	console.log(data.registrationId);
});
```

## push.on('notification', callback)

O evento `notification` será acionado a cada notificação push recebida pelo GCM no aparelho.

### Parâmetros do callback

Parâmetro | Tipo | Descrição
----------|------|---------
`data.message`| `string` | Texto da mensagem da notificação vinda do GCM.
`data.title` | `string` | Título da mensagem da notificação vinda do GCM.
`data.internal` | `string` | Texto interno para ser utilizado pelo aplicativo.

### Exemplo

```javascript
push.on('notification', function(data) {
	console.log(data.message);
	console.log(data.title);
	console.log(data.internal);
});
```
## push.on('error', callback)

O evento `error` será acionado a cada erro interno.

### Parâmetros do callback

Parâmetro | Tipo | Descrição
----------|------|---------
`e` | `Error` | Objeto padrão do Javascript descrevendo o erro.

### Exemplo

```javascript
push.on('error', function(e) {
	console.log(e.message);
});
```

## push.off(event, callback)

Remove um `callback` previamente registrado para um evento.

### Parametros

Parâmetro | Tipo | Padrão | Descrição
----------|------|--------|----------
`event` | `string` | | Tipo do evento. Os tipos são os mesmos da função `push.on`
`callback` | `Function` | | O mesmo `callback` usado no `push.on`

### Example

```javascript
var callback = function(data){ /*...*/};

// Adiciona função a ser chamada no evento de notification
push.on('notification', callback);

// Remove função para o evento de notification
push.off('notification', callback);
```

**Atenção:** Como mostrado no evento, é necessário armazenar a função se for necessário removê-la futuramente.

## push.unregister(successHandler, errorHandler)

O método `unregister` é utilizado quando a aplicação não desejar mais receber as notificações. Este método irá desregistrar todas as funções utilizadas no `push.on`.

### Parametros

Parâmetro | Tipo | Padrão | Descrição
----------|------|--------|----------
`successHandler` | `Function` | | É chamado quando a API desregistra com sucesso.
`errorHandler` | `Function` | | É chamado caso a API encontre um erro no desregistro.

### Exemplo

```javascript
push.unregister(function() {
	console.log('success');
}, function() {
	console.log('error');
});
```
