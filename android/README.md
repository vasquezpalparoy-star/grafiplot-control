# TIENDA GRAFIPLOT para Android

Nombre en Android: **TIENDA GRAFIPLOT**. Identificador independiente: `com.grafiplot.tienda`; se instala junto a la app original y no la reemplaza.

Aplicación para trabajadores: escanear códigos de barras y QR, ingresar un código, buscar por nombre, consultar precios, existencias, fotos y ubicación, y agregar productos.

## Instalación

En GitHub → Actions → **APK Tienda Grafiplot**, abre una ejecución terminada correctamente y descarga **Tienda-Grafiplot-APK**. Descomprime el ZIP e instala `Tienda-Grafiplot.apk` en Android 7.0 o posterior. Permite la instalación desde esa fuente cuando Android lo solicite. La cámara se solicita solo al escanear. Si se deniega, puedes ingresar el código manualmente.

La primera apertura requiere Internet. Se usa el mismo proyecto Firebase `imventario-105b7`, autenticación anónima y colección `inventory` que `index.html`; no se crea otro inventario. Firebase debe permitir la autenticación anónima y las operaciones correspondientes en sus reglas existentes. Esta app respeta esas reglas y no las modifica. Si falla la conexión o se rechaza una escritura, se informa al usuario.

Los cambios del inventario llegan por una suscripción Firestore. Los datos consultados y escrituras pendientes se conservan mediante IndexedDB; las escrituras pendientes se envían cuando regresa la conexión. Se indica **Cambios pendientes** hasta que Firebase confirma la escritura. Los datos y fotos que nunca se han descargado requieren conexión. La página de escritorio mantiene su comportamiento existente: sus guardados locales fallidos no se sincronizan automáticamente con esta app.

La app incluye su interfaz y SDK Firebase 10.13.1 dentro de la APK. No requiere desplegar una nueva página ni abrir el navegador. Cambios del inventario se actualizan en vivo; cambios de interfaz requieren instalar una nueva APK. El icono procede de la imagen facilitada por el propietario.

## Compilación

JDK 17, Android SDK 35 y Gradle 8.9:

```sh
cd android
gradle lintDebug assembleDebug
```

Resultado: `app/build/outputs/apk/debug/app-debug.apk`.

La APK generada por Actions es una versión de prueba firmada con la clave de depuración del ejecutor. No es una publicación en Google Play. Para distribuir actualizaciones instalables sobre la versión anterior, configura una clave de firma estable para una compilación release y conserva esa clave; las claves de depuración de ejecuciones diferentes pueden ser distintas.

## Comprobaciones

Antes de usar en producción, instalar en un teléfono y verificar:

1. Escanear un código existente y comprobar que abre el producto correcto.
2. Denegar la cámara y consultar por código manual.
3. Buscar por nombre con y sin tildes; consultar fotos y ubicación.
4. Agregar un producto de prueba y comprobar que aparece en el escritorio.
5. Cambiar un producto desde el escritorio y comprobar su actualización en el teléfono.
6. Desconectar Internet después de ingresar, agregar un producto y comprobar el indicador pendiente; reconectar y verificar su llegada al escritorio.
7. Confirmar que un rechazo de Firebase se informa sin mostrarlo como sincronizado.

No se ha cambiado la autorización de Firebase: las cuentas anónimas tienen solo los permisos que permitan las reglas actuales. Para distinguir permisos de administrador y trabajador, se necesita agregar cuentas/roles al proyecto Firebase.
