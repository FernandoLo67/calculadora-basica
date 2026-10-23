package com.fernando.calculadora;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de {@link Calculadora}.
 *
 * <p>Dos bloques distintos, y la diferencia importa:
 *
 * <ul>
 *   <li><b>Desbordes</b> — el defecto corregido. Son cosas que la clase tiene
 *       que atrapar.</li>
 *   <li><b>Punto flotante</b> — hechos del tipo {@code double} que la clase
 *       <i>no</i> puede arreglar. Se fijan en pruebas para que esten
 *       documentados y no se descubran por accidente en produccion.</li>
 * </ul>
 */
class CalculadoraTest {

    private Calculadora calculadora;

    @BeforeEach
    void prepararCalculadora() {
        calculadora = new Calculadora();
    }

    // ---------------------------------------------------------------
    //  Las cuatro operaciones
    // ---------------------------------------------------------------

    @Test
    void lasCuatroOperacionesConEnteros() {
        assertEquals(7.0, calculadora.sumar(3, 4));
        assertEquals(-1.0, calculadora.restar(3, 4));
        assertEquals(12.0, calculadora.multiplicar(3, 4));
        assertEquals(0.75, calculadora.dividir(3, 4));
    }

    @Test
    void losNegativosYelCeroFuncionan() {
        assertEquals(-7.0, calculadora.sumar(-3, -4));
        assertEquals(1.0, calculadora.restar(-3, -4));
        assertEquals(12.0, calculadora.multiplicar(-3, -4));
        assertEquals(0.0, calculadora.multiplicar(0, 5));
        assertEquals(0.0, calculadora.dividir(0, 5));
    }

    // ---------------------------------------------------------------
    //  Division entre cero
    // ---------------------------------------------------------------

    @Test
    void dividirEntreCeroLanzaExcepcion() {
        assertThrows(ArithmeticException.class, () -> calculadora.dividir(5, 0));
        assertThrows(ArithmeticException.class, () -> calculadora.dividir(0, 0));
    }

    /**
     * {@code -0.0} tambien. En punto flotante {@code -0.0 == 0.0} es
     * {@code true} aunque sean dos valores distintos con dos representaciones
     * distintas: sin esta comprobacion, {@code 1 / -0.0} daria
     * {@code -Infinity} en vez de {@code Infinity}.
     */
    @Test
    void dividirEntreCeroNegativoTambienLanzaExcepcion() {
        assertThrows(ArithmeticException.class, () -> calculadora.dividir(5, -0.0));
        assertEquals(-0.0, -0.0);
        assertTrue(-0.0 == 0.0, "son iguales al comparar...");
        assertNotEquals(Double.doubleToRawLongBits(-0.0), Double.doubleToRawLongBits(0.0),
                "...pero no son el mismo valor");
    }

    // ---------------------------------------------------------------
    //  Desborde: donde estaba el defecto
    // ---------------------------------------------------------------

    /**
     * LA PRUEBA QUE DOCUMENTA EL DEFECTO CORREGIDO.
     *
     * <p>La version anterior devolvia {@code Infinity} sin decir nada.
     * {@code Infinity} es un {@code double} valido: no lanza, se imprime, y
     * sigue viaje contagiando todo lo que toca.
     */
    @Test
    void elDesbordeLanzaExcepcionEnVezDeDevolverInfinito() {
        assertThrows(ArithmeticException.class,
                () -> calculadora.sumar(Double.MAX_VALUE, Double.MAX_VALUE));
        assertThrows(ArithmeticException.class,
                () -> calculadora.restar(-Double.MAX_VALUE, Double.MAX_VALUE));
        assertThrows(ArithmeticException.class,
                () -> calculadora.multiplicar(1e200, 1e200));
        assertThrows(ArithmeticException.class,
                () -> calculadora.multiplicar(-1e200, 1e200));
        assertThrows(ArithmeticException.class,
                () -> calculadora.dividir(1e200, 1e-200));
    }

    /** Y lo que si cabe, pasa: el limite no es conservador de mas. */
    @Test
    void loQueCabeEnUnDoubleNoSeRechaza() {
        assertEquals(Double.MAX_VALUE, calculadora.sumar(Double.MAX_VALUE, 0));
        assertEquals(Double.MAX_VALUE, calculadora.multiplicar(Double.MAX_VALUE, 1));
        assertDoesNotThrow(() -> calculadora.multiplicar(1e150, 1e150));
    }

    /**
     * {@code NaN} y los infinitos tampoco entran. {@code NaN} es el peor de
     * todos porque se escapa de cualquier comparacion: {@code NaN != NaN}.
     */
    @Test
    void lasEntradasNoFinitasSeRechazan() {
        assertThrows(IllegalArgumentException.class, () -> calculadora.sumar(Double.NaN, 5));
        assertThrows(IllegalArgumentException.class, () -> calculadora.sumar(5, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.multiplicar(Double.POSITIVE_INFINITY, 2));
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.dividir(Double.NEGATIVE_INFINITY, 2));
    }

    /**
     * DESCUBIERTO ESCRIBIENDO ESTAS PRUEBAS.
     *
     * <p>{@code assertEquals(double, double)} <b>no compara con {@code ==}</b>.
     * Compara los bits, igual que {@code Double.equals}. Y las dos semanticas
     * estan invertidas justo en los dos casos raros:
     *
     * <table border="1">
     *   <caption>Comparar dos {@code double} de tres maneras</caption>
     *   <tr><th></th><th>{@code ==}</th><th>{@code assertEquals(a, b)}</th><th>{@code assertEquals(a, b, 0.0)}</th></tr>
     *   <tr><td>{@code NaN} vs {@code NaN}</td><td>distintos</td><td><b>iguales</b></td><td>distintos</td></tr>
     *   <tr><td>{@code 0.0} vs {@code -0.0}</td><td>iguales</td><td><b>distintos</b></td><td>iguales</td></tr>
     * </table>
     *
     * <p>La consecuencia practica es que agregarle un {@code delta} de
     * {@code 0.0} a un {@code assertEquals} no es redundante: <b>cambia la
     * semantica</b>, porque la sobrecarga de tres argumentos compara
     * {@code Math.abs(esperado - real) <= delta}, que es aritmetica y no bits.
     *
     * <p>Se descubrio porque {@code multiplicar(0, -1)} devuelve {@code -0.0} y
     * la prueba de propiedades fallaba comparandolo contra {@code 0}. El defecto
     * estaba en la prueba, no en la calculadora.
     */
    @Test
    void assertEqualsDeDoublesNoUsaElOperadorIgual() {
        // NaN: distinto con ==, igual para assertEquals
        assertFalse(Double.NaN == Double.NaN);
        assertEquals(Double.NaN, Double.NaN);

        // cero negativo: igual con ==, distinto para assertEquals
        assertTrue(0.0 == -0.0);
        assertNotEquals(Double.doubleToRawLongBits(0.0), Double.doubleToRawLongBits(-0.0));
        assertEquals(0.0, -0.0, 0.0, "con delta 0.0 si son iguales");

        // y de donde salio: multiplicar por cero conserva el signo
        assertEquals(-0.0, calculadora.multiplicar(0, -1));
        assertEquals(0.0, calculadora.multiplicar(0, 1));
    }

    // ---------------------------------------------------------------
    //  Punto flotante: hechos del tipo, no defectos de la clase
    // ---------------------------------------------------------------

    /**
     * {@code 0.1} y {@code 0.2} no tienen representacion exacta en binario, del
     * mismo modo que {@code 1/3} no la tiene en decimal. La suma acumula los dos
     * errores.
     *
     * <p>Por eso comparar resultados de punto flotante con {@code ==} es un
     * error, y por eso el dinero no se guarda en {@code double}: ver
     * {@code cajero-automatico-simple} en este portafolio.
     */
    @Test
    void unaDecimaMasDosDecimasNoEsTresDecimas() {
        double resultado = calculadora.sumar(0.1, 0.2);

        assertNotEquals(0.3, resultado, "no son iguales...");
        assertEquals(0.30000000000000004, resultado, "...el resultado real es este");
        assertEquals(0.3, resultado, 1e-15, "...y con tolerancia si coinciden");
    }

    /**
     * La suma de {@code double} <b>no es asociativa</b>. Cambiar el orden de los
     * parentesis cambia el resultado, porque cada paso redondea a 53 bits.
     *
     * <p>Es la razon por la que sumar un arreglo grande de izquierda a derecha
     * da un resultado distinto que sumarlo en otro orden, y por la que las
     * sumas en paralelo no son reproducibles salvo que se fije el orden.
     */
    @Test
    void laSumaDeDoublesNoEsAsociativa() {
        double izquierda = calculadora.sumar(calculadora.sumar(0.1, 0.2), 0.3);
        double derecha = calculadora.sumar(0.1, calculadora.sumar(0.2, 0.3));

        assertNotEquals(izquierda, derecha);
        assertEquals(0.6000000000000001, izquierda);
        assertEquals(0.6, derecha);
    }

    /**
     * Cancelacion catastrofica: restar dos numeros muy parecidos destruye las
     * cifras significativas.
     *
     * <p>{@code 1e16 + 1} no se puede representar — a esa magnitud los
     * {@code double} consecutivos estan a distancia 2 — asi que el {@code +1}
     * se pierde antes de restar y el resultado es {@code 0} en vez de
     * {@code 1}. Con {@code +2} si funciona, porque 2 es exactamente el paso.
     *
     * <p>Un {@code double} tiene 53 bits de mantisa: los enteros son exactos
     * solo hasta 2^53. Mas alla, sumar 1 puede no cambiar nada.
     */
    @Test
    void restarNumerosParecidosPierdeTodasLasCifras() {
        assertEquals(0.0, calculadora.restar(calculadora.sumar(1e16, 1), 1e16),
                "el +1 se perdio antes de la resta");
        assertEquals(2.0, calculadora.restar(calculadora.sumar(1e16, 2), 1e16),
                "el +2 si sobrevive: 2 es el paso a esa magnitud");

        double dosALa53 = 9007199254740992.0;
        assertEquals(dosALa53, calculadora.sumar(dosALa53, 1),
                "a partir de 2^53 sumar 1 no cambia nada");
        assertNotEquals(dosALa53, calculadora.sumar(dosALa53, 2));
    }

    /**
     * Subdesborde: el resultado exacto es distinto de cero pero es tan chico que
     * el {@code double} mas cercano es cero. Multiplicar dos numeros positivos y
     * obtener cero es raro pero legal.
     *
     * <p>No se lanza excepcion — cero es un resultado finito y perfectamente
     * usable — pero conviene saber que puede pasar.
     */
    @Test
    void elSubdesbordeDevuelveCeroSinAvisar() {
        assertEquals(0.0, calculadora.multiplicar(1e-200, 1e-200));
        assertEquals(0.0, calculadora.dividir(1e-300, 1e300));
    }

    // ---------------------------------------------------------------
    //  Propiedades que si se cumplen
    // ---------------------------------------------------------------

    /**
     * Con enteros chicos — los que caben exactos en un {@code double} — la
     * aritmetica si es exacta y las propiedades algebraicas se cumplen.
     */
    @Test
    void conEnterosChicosLaAritmeticaEsExacta() {
        for (int a = -50; a <= 50; a++) {
            for (int b = -50; b <= 50; b++) {
                // delta 0.0 y no la version de dos argumentos: hace falta la
                // semantica de == para que 0.0 y -0.0 cuenten como iguales.
                assertEquals(a + b, calculadora.sumar(a, b), 0.0);
                assertEquals(a * b, calculadora.multiplicar(a, b), 0.0);
                assertEquals(calculadora.sumar(a, b), calculadora.sumar(b, a), 0.0,
                        "la suma si es conmutativa");
                if (b != 0) {
                    assertEquals(a, calculadora.multiplicar(calculadora.dividir(a, b), b), 1e-9,
                            "dividir y volver a multiplicar");
                }
            }
        }
    }

    /** Restar es sumar el opuesto, siempre. */
    @Test
    void restarEsSumarElOpuesto() {
        for (int a = -20; a <= 20; a++) {
            for (int b = -20; b <= 20; b++) {
                assertEquals(calculadora.restar(a, b), calculadora.sumar(a, -b), 0.0);
            }
        }
    }
}
