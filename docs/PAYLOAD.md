# Detalhes das notificações

- [Android](#android)
    - [Empilhamento de notificações](#empilhamento-de-notificações)
    - [Notificações em background](#notificações-em-background)
    - [Notificações permanentes](#notificações-permanentes)


# Android

## Empilhamento de notificações

Quando as notificações são enviadas sem um identificador, o plugin irá agrupá-las para melhorar a visualização do usuário.

Assim, para evitar este efeito, basta acrescentar um inteiro único no campo `notId` da notificação.

### Exemplo

```php
<?php
$mensagem = array(
    'message' => 'Mensagem da notificação',
    'title' => 'Título da notificação',
    'internal' => 'Conteúdo para ser usada internamente'
    'notId' => (int)microtime() + floor(rand()*10000)    
);
```

## Notificações em background

Caso seja necessário chamar a função utilizada no `notification` assim que a notificação for recebida quando o aplicativo está em background, basta enviar, como parâmetro na mensagem, a opção `backgroundProcess` com `true`.

Quando o usuário clicar na notificação, seu conteúdo não será processado novamente.

### Exemplo

```php
<?php
$mensagem = array(
    'message' => 'Mensagem da notificação',
    'title' => 'Título da notificação',
    'internal' => 'Conteúdo para ser usada internamente'
    'backgroundProcess' => true   
);
?>
```

## Notificações permanentes

Com está opção ativada, o usuário não será capaz de remover a notificação e, consequentemente, seu conteúdo será processado quando houver o toque, caso a opção anterior esteja desativada.

### Exemplo

```php
<?php
$mensagem = array(
    'message' => 'Mensagem da notificação',
    'title' => 'Título da notificação',
    'internal' => 'Conteúdo para ser usada internamente'
    'ongoing' => true   
);
?>
```
