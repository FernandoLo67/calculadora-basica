package com.fernando.calculadora;

/**
 * Las cuatro operaciones aritmeticas sobre {@code double}, con los desbordes
 * detectados en vez de escondidos.
 *
 * <h2>El defecto: el desborde era silencioso</h2>
 *
 * La version anterior solo comprobaba la division entre cero. Todo lo demas
 * pasaba tal cual:
 *
 * <pre>{@code
 * calculadora.multiplicar(1e200, 1e200);   // devolvia Infinity
 * calculadora.sumar(MAX_VALUE, MAX_VALUE); // devolvia Infinity
 * calculadora.sumar(Double.NaN, 5);        // devolvia NaN
 * }</pre>
 *
 * <p>{@code Infinity} y {@code NaN} son valores validos de {@code double}: no
 * lanzan nada, se imprimen sin problema y siguen viaje por el resto del
 * programa. Y son contagiosos — cualquier operacion que toque uno devuelve otro
 * — asi que el error aparece muy lejos del lugar donde se produjo. Peor que un
 * error: un error <i>diferido</i>.
 *
 * <p>Ahora las entradas tienen que ser numeros finitos y el resultado tambien.
 * Si una operacion entre numeros finitos da algo que no lo es, hubo un desborde
 * y se lanza {@link ArithmeticException}.
 *
 * <h2>Por que sigue usando {@code double} y no {@code BigDecimal}</h2>
 *
 * Una calculadora de proposito general opera sobre magnitudes cualesquiera:
 * raices, promedios, medidas fisicas. Ahi {@code double} es la herramienta
 * correcta — rapida, de rango enorme, y con el error relativo acotado a un ulp
 * por operacion.
 *
 * <p>Donde {@code double} <b>no</b> sirve es en dinero, porque ahi los valores
 * son decimales exactos y el error se acumula centavo a centavo. Ese caso esta
 * resuelto con {@code BigDecimal} en el proyecto
 * {@code cajero-automatico-simple} de este portafolio.
 *
 * <p>Lo que esta clase no puede arreglar es la aritmetica de punto flotante en
 * si: {@code 0.1 + 0.2} sigue dando {@code 0.30000000000000004}, la suma sigue
 * sin ser asociativa y restar dos numeros parecidos sigue perdiendo cifras. Eso
 * no son defectos de esta clase, son propiedades del tipo, y las pruebas las
 * fijan explicitamente para que nadie las descubra por accidente.
 */
public class Calculadora {

    /**
     * @return {@code a + b}
     * @throws IllegalArgumentException si alguna entrada no es finita
     * @throws ArithmeticException      si el resultado desborda el rango de un {@code double}
     */
    public double sumar(double a, double b) {
        return comprobar(validar(a) + validar(b), "suma");
    }

    /**
     * @return {@code a - b}
     * @throws IllegalArgumentException si alguna entrada no es finita
     * @throws ArithmeticException      si el resultado desborda el rango de un {@code double}
     */
    public double restar(double a, double b) {
        return comprobar(validar(a) - validar(b), "resta");
    }

    /**
     * @return {@code a * b}
     * @throws IllegalArgumentException si alguna entrada no es finita
     * @throws ArithmeticException      si el resultado desborda el rango de un {@code double}
     */
    public double multiplicar(double a, double b) {
        return comprobar(validar(a) * validar(b), "multiplicacion");
    }

    /**
     * Divide {@code a} entre {@code b}.
     *
     * <p>La comprobacion {@code b == 0} tambien atrapa {@code -0.0}, porque en
     * punto flotante {@code -0.0 == 0.0} es {@code true} aunque sean dos valores
     * distintos. Sin esa comprobacion, dividir entre {@code -0.0} daria
     * {@code -Infinity} en vez de {@code Infinity}: dos formas distintas de
     * estar mal.
     *
     * @return {@code a / b}
     * @throws ArithmeticException      si {@code b} es cero, o si el resultado desborda
     * @throws IllegalArgumentException si alguna entrada no es finita
     */
    public double dividir(double a, double b) {
        validar(a);
        validar(b);
        if (b == 0) {
            throw new ArithmeticException("No se puede dividir entre cero");
        }
        return comprobar(a / b, "division");
    }

    /**
     * Comprueba que un operando sea un numero real utilizable.
     *
     * <p>{@code Double.isFinite} cubre de una sola vez {@code NaN},
     * {@code +Infinity} y {@code -Infinity}. Escribirlo como
     * {@code x != Double.NaN} no funcionaria: {@code NaN} no es igual a nada, ni
     * siquiera a si mismo, asi que esa comparacion es {@code false} siempre.
     *
     * @param x el operando
     * @return el mismo {@code x}, para poder encadenarlo
     * @throws IllegalArgumentException si no es finito
     */
    private double validar(double x) {
        if (!Double.isFinite(x)) {
            throw new IllegalArgumentException(
                    "Los operandos tienen que ser numeros finitos, y llego " + x);
        }
        return x;
    }

    /**
     * Comprueba que el resultado siga siendo finito.
     *
     * <p>Como las entradas ya se validaron, un resultado no finito solo puede
     * venir de un desborde: el valor exacto existe pero no cabe en un
     * {@code double}.
     *
     * @param resultado  el valor calculado
     * @param operacion  nombre de la operacion, para el mensaje
     * @return el mismo resultado
     * @throws ArithmeticException si desbordo
     */
    private double comprobar(double resultado, String operacion) {
        if (!Double.isFinite(resultado)) {
            throw new ArithmeticException(
                    "El resultado de la " + operacion + " excede el rango de un double");
        }
        return resultado;
    }
}
