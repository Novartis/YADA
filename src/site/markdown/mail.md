# Mail Specification

## JSON Object

Mail parameters must be passed as a stringified version of the following JSON object. The values in the JSON object must conform to the specifications of [javax.mail.Message](http://docs.oracle.com/javaee/1.4/api/javax/mail/Message.html)

```javascript
{
  "from" : "",
  "to"   : "",
  "cc"   : "",
  "subject" : "",
  "content" : ""
}
```

## Javascript Example

```javascript
var mail = {
        from:$('input[name="mailer-from"]').val(),
        to:$('input[name="mailer-to"]').val(),
        cc:$('input[name="mailer-cc"]:checked').length == 1 ? $('input[name="mailer-from"]').val() : '',
        subject:$('input[name="mailer-subj"]').val(),
        content:$('textarea[name="mailer-body"]').val()
};

// perform the send action
$.ajax({
    url:'/yada.jsp',
    data:{
        qname:'YADA default',
        mail: JSON.stringify(mail),
        plugin: 'EmailBypassPlugin'
    },
    success: function(data) {
        if (data == '1')
            alert('Message sent successfully.');
        else
            alert('There was a problem sending your message.  Please try again.');
    }
});
```

## Behind the Scenes

`EmailBypassPlugin.engage()`
 
```java
JSONObject json    = new JSONObject(svcParams.getMail());  
String     from    = json.getString("from");  
String     to      = json.getString("to");  
String     cc      = json.getString("cc");  
String     subject = json.getString("subject");  
String     content = json.getString("content");  
Session session = new MailUtils().getSession();  
result = String.valueOf(MailUtils.sendMessage(session, from, to, cc, subject, content));  
```

`MailUtils.sendMessage()`

```java
message = new MimeMessage(session);
message.setFrom(new InternetAddress(from));
InternetAddress toList[] = InternetAddress.parse(to);
message.setRecipients(Message.RecipientType.TO, toList);
InternetAddress ccList[] = InternetAddress.parse(cc);
message.setRecipients(Message.RecipientType.CC, ccList);
message.setSubject(subject);
message.setContent(content, "text/plain");
l.debug("Sending message...");
Transport.send(message);
```

