# Calculadora Basica

Las cuatro operaciones aritmeticas por consola, sobre `double`.

Tenia un defecto que devuelve un valor perfectamente valido de Java y perfectamente inutil como respuesta: **el desborde era silencioso**.

## El defecto: `Infinity` no es un error, es un resultado

La version anterior solo comprobaba la division entre cero:

```java
public double multiplicar(double a, double b) {
    return a * b;          // sin comprobar nada
}
```

| llamada | antes | ahora |
|---|---|---|
| `multiplicar(1e200, 1e200)` | **`Infinity`** | `ArithmeticException` |
| `sumar(MAX_VALUE, MAX_VALUE)` | **`Infinity`** | `ArithmeticException` |
| `sumar(Double.NaN, 5)` | **`NaN`** | `IllegalArgumentException` |

`Infinity` y `NaN` son valores legitimos de `double`. No lanzan nada, se imprimen sin problema, se guardan en la base de datos sin problema, y **son contagiosos**: cualquier operacion que toque uno devuelve otro. El error aparece a diez capas de distancia del lugar donde se produjo, convertido en un `NaN` sin origen.

Es peor que un error: es un error *diferido*.

Ahora las entradas tienen que ser finitas y el resultado tambien. Como las entradas ya se validaron, un resultado no finito solo puede venir de un desborde.

Detalle que aparecio al arreglarlo: `Double.parseDouble("NaN")` devuelve `NaN` y `Double.parseDouble("Infinity")` devuelve infinito. Son textos que el usuario puede teclear, asi que el menu tiene que atrapar tambien `IllegalArgumentException`.

## `-0.0` existe, y `-0.0 == 0.0`

```java
if (b == 0) throw new ArithmeticException("No se puede dividir entre cero");
```

Esa comparacion atrapa tambien `-0.0`, porque el operador `==` los considera iguales aunque sean dos valores distintos con dos representaciones binarias distintas. Convenientemente: sin ella, `1 / -0.0` daria `-Infinity` en vez de `Infinity`. Dos formas distintas de estar mal.

## Lo que esta clase no puede arreglar

Hay un bloque entero de pruebas dedicado a cosas que **no son defectos de la calculadora sino propiedades del tipo `double`**. Estan fijadas en pruebas para que queden documentadas y nadie las descubra por accidente:

**`0.1 + 0.2` da `0.30000000000000004`.** Ni `0.1` ni `0.2` tienen representacion exacta en binario, igual que `1/3` no la tiene en decimal. Por eso comparar resultados de punto flotante con `==` es un error.

**La suma no es asociativa.**

```
(0.1 + 0.2) + 0.3 = 0.6000000000000001
 0.1 + (0.2 + 0.3) = 0.6
```

Cada paso redondea a 53 bits. Por eso sumar un arreglo grande en distinto orden da distinto resultado, y por eso las sumas en paralelo no son reproducibles salvo que se fije el orden.

**Restar numeros parecidos destruye las cifras.**

```
(1e16 + 1) - 1e16 = 0.0     <- deberia ser 1
(1e16 + 2) - 1e16 = 2.0     <- este si
```

A la magnitud `1e16` los `double` consecutivos estan a distancia 2, asi que el `+1` se pierde *antes* de la resta. Los enteros son exactos solo hasta 2^53 = 9007199254740992; a partir de ahi, sumar 1 puede no cambiar nada.

**El subdesborde devuelve cero sin avisar.** `1e-200 * 1e-200` da `0.0`. Dos numeros positivos que se multiplican y dan cero. No se lanza excepcion porque cero es un resultado finito y usable, pero conviene saber que puede pasar.

## Por que sigue siendo `double` y no `BigDecimal`

Una calculadora de proposito general opera sobre magnitudes cualesquiera. Ahi `double` es la herramienta correcta: rango enorme, rapida, y con el error relativo acotado a un ulp por operacion.

Donde `double` **no** sirve es en dinero, porque ahi los valores son decimales exactos y el error se acumula centavo a centavo. Ese caso esta resuelto con `BigDecimal` en el proyecto `cajero-automatico-simple` de este portafolio, con la prueba que lo demuestra: diez depositos de diez centavos tienen que dar exactamente un peso.

Son dos problemas distintos con dos respuestas distintas. Usar `BigDecimal` para todo es tan equivocado como usar `double` para dinero.

## Lo que se descubrio escribiendo las pruebas

`assertEquals(double, double)` **no compara con `==`**. Compara los bits, igual que `Double.equals`. Y las dos semanticas estan invertidas justo en los dos casos raros:

| | `==` | `assertEquals(a, b)` | `assertEquals(a, b, 0.0)` |
|---|---|---|---|
| `NaN` vs `NaN` | distintos | **iguales** | distintos |
| `0.0` vs `-0.0` | iguales | **distintos** | iguales |

Aparecio porque `multiplicar(0, -1)` devuelve `-0.0` — la multiplicacion conserva el signo — y la prueba de propiedades fallaba comparandolo contra `0`. El defecto estaba en la prueba, no en la calculadora.

La consecuencia practica es que **agregarle un `delta` de `0.0` a un `assertEquals` no es redundante: cambia la semantica**, porque la sobrecarga de tres argumentos compara `Math.abs(esperado - real) <= delta`, que es aritmetica y no bits. Las pruebas de propiedades lo usan a proposito, con un comentario que explica por que.

## Las 14 pruebas

| grupo | que cubre |
|---|---|
| Operaciones | Las cuatro con enteros, negativos y cero |
| Division entre cero | Incluido `-0.0`, con la comprobacion de que son valores distintos |
| **Desborde** | El defecto corregido en las cuatro operaciones; lo que si cabe no se rechaza; `NaN` e infinitos de entrada |
| Punto flotante | `0.1+0.2`; no asociatividad; cancelacion catastrofica; 2^53; subdesborde |
| Propiedades | Exactitud con enteros chicos (10201 casos); conmutatividad; restar = sumar el opuesto |
| Sobre JUnit | Las tres semanticas de igualdad de `double` |

## Estructura

```
src/main/java/com/fernando/calculadora/
├── Calculadora.java   Las cuatro operaciones y la deteccion de desborde.
└── Main.java          Solo lee del teclado y escribe en pantalla.
src/test/java/com/fernando/calculadora/
└── CalculadoraTest.java   14 pruebas.
```

El menu imprime el resultado con `System.out.println` y no con `%.4f`: redondear a cuatro decimales escondería justamente lo que este proyecto quiere mostrar.

## Como ejecutar

```bash
mvn package
java -jar target/calculadora-basica-1.0.0.jar
```

## Como correr las pruebas

```bash
mvn test
```
