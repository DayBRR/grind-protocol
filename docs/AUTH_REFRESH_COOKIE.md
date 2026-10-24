# BUG-AUTH-003: ruta de la cookie de refresh

La cookie `refresh_token` utiliza `Path=/api/auth`, incluida la parte `/api`
de `server.servlet.context-path`. El navegador puede enviarla a
`/api/auth/refresh`, `/api/auth/logout` y los demas endpoints de autenticacion.
Login, registro y refresh la emiten con esa misma ruta; logout y logout global
la eliminan con la misma ruta y `Max-Age=0`.

Se mantienen `HttpOnly=true`, `SameSite=Lax`, `Secure=false`, sin atributo
`Domain`, y una duracion de 14 dias. El cambio no amplía el envio a todos los
endpoints de la API. `Secure=false` conserva el comportamiento HTTP existente;
esta correccion no cambia la politica de HTTPS del despliegue.

Si se cambia el context path o un proxy publica otra ruta externa, debe
alinearse `security.cookie.path` con la ruta que ve el navegador. Un override
de despliegue que conserve `/auth` debe actualizarse tambien.

Las cookies antiguas con `Path=/auth` no se migran ni se eliminan con una
cookie de otra ruta. Una sesion que solo conserve esa cookie necesitara un
nuevo login para recibir la cookie corregida. La cookie antigua no se envia
a `/api/auth/refresh` y caducara segun su duracion original.

Los tests comprueban el encabezado `Set-Cookie` con la configuracion real y
servicios simulados, ademas del registro real sobre H2. Queda pendiente la
validacion en navegador del envio de la cookie tras expirar el access token.
La coordinacion de refresh concurrentes y las condiciones de rotacion y
revocacion quedan fuera de esta intervencion.
