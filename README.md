# Device To Device Streaming en dispositivos móviles
Este proyecto es un Trabajo de Fin de Grado del Grado en Ingeniería Informática, Facualtad de Ingeniería Informática de la Universidad Complutense de Madrid

## Autores

- Noel José Algora Igual
- Iván Gulyk

## Director

- Simon Pickin

## Resumen

El objetivo principal de este proyecto ha sido desarrollar una aplicación para
dispositivos móviles capaz de crear una red distribuida, y de retransmitir
vídeo en streaming directamente desde la cámara y micrófono de uno o
varios dispositivos de la red. La aplicación propuesta debía poder crear
una red infinita por interconexión de dispositivos cercanos, todo esto sin
usar la infraestructura de red de los operadores de telecomunicaciones.

En el transcurso del proyecto mostramos que, debido a las limitaciones
en la implementación de la tecnología disponible, actualmente no es posible
lograr del todo este objetivo. Por esta razón, desarrollamos una aplicación
con funcionalidad reducida, pero capaz de transmitir y reproducir streaming a través de una pequeña red distribuida de dispositivos conectados
mediante la tecnología WiFi Direct. La aplicación permite distribuir el
streaming por medio de multihopping, de tal forma que vaya pasando de
un dispositivo a otro en cadena hasta llegar a su destino. Sin embargo
el estado actual de la tecnología implementada en los dispositivos móviles
solo nos permite realizar dos saltos, es decir, conectar tres dispositivos en
cadena.
