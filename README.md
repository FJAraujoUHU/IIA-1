# IIA-1: Diseño de un sistema DSL

## Características
#### Funcional:
Todas las tareas, conectores y demás elementos han sido testados usando JUnit (Con el proyecto Café implementado como una unidad JUnit y como ejemplo de uso de la librería)
#### Concurrente:
Cada tarea se ejecuta de manera independiente en hilos
#### Asíncrono:
Las tareas y conectores se comunican de forma asíncrona, con un sistema de Slots basado en Pipes
#### Completamente modular:
El uso extensivo de interfaces, clases abstractas y herencia garantiza el fácil uso de la librería para el diseño de cualquier solución de integración, e incluso el desarrollo de tareas adicionales que expandan el potencial y las capacidades de la librería.
#### Código a medida:
Todo el código se basa en las librerías básicas de Java (A excepción del Driver de MySQL, por razones evidentes). Si Java no ofrece un recurso, se ha implementado código a medida para la solución.
#### Código moderno:
Se han aprovechado las capacidades que ofrece Java moderno, como el lenguaje funcional, polimorfismo y expresiones lambda.
#### Documentación extensa:
La mayoría de las clases cuentan con Javadocs que detallan el uso y funcionamiento, y abundan los comentarios dentro del código.
