# TextApi
 An application that serves as a server to sms api requests.
# How to use?
## PYTHON
```python
from requests import get 
resp = get('https://{your-ip}:8080/?phone=reciever\'s-number&message=your-message')
print("Sent successfully" if resp.status_code == 200 else "Message not sent")
```
## VISUAL BASIC
```vb
Dim request As HttpWebRequest = HttpWebRequest.CreateHttp("https://{your-ip}:8080/?phone=reciever\'s-number&message=your-message")
        Dim response As HttpWebResponse
        Try
            response = DirectCast(request.GetResponse(), HttpWebResponse)
            If response.StatusCode = 200 Then
	Debug.WriteLine("Message sent!")
            Else
 	Debug.WriteLine("Message not sent!")
            End If
        Catch ex As WebException
            response = DirectCast(ex.Response, HttpWebResponse)
        End Try
```
