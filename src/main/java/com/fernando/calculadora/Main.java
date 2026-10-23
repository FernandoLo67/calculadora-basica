package com.fernando.calculadora;

import java.util.Scanner;

/**
 * Menu de consola de la calculadora.
 *
 * <p>Solo lee del teclado y escribe en pantalla. Toda la aritmetica y toda la
 * validacion viven en {@link Calculadora}, que es lo que permite probarlas sin
 * simular una consola.
 */
public class Main {

    public static void main(String[] args) {
        Calculadora calculadora = new Calculadora();
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        System.out.println("=== Calculadora Basica ===");
        while (!salir) {
            mostrarMenu();
            int opcion = leerOpcion(scanner);

            if (opcion == 5) {
                salir = true;
                System.out.println("Hasta luego!");
                continue;
            }
            if (opcion < 1 || opcion > 5) {
                System.out.println("Opcion invalida. Intenta de nuevo.\n");
                continue;
            }

            try {
                double a = leerNumero(scanner, "Ingresa el primer numero: ");
                double b = leerNumero(scanner, "Ingresa el segundo numero: ");
                double resultado;
                switch (opcion) {
                    case 1:
                        resultado = calculadora.sumar(a, b);
                        break;
                    case 2:
                        resultado = calculadora.restar(a, b);
                        break;
                    case 3:
                        resultado = calculadora.multiplicar(a, b);
                        break;
                    case 4:
                        resultado = calculadora.dividir(a, b);
                        break;
                    default:
                        resultado = 0.0;
                        break;
                }
                // %s y no %.4f: %.4f redondea, y la gracia de esta calculadora
                // es que el resultado real de 0.1 + 0.2 se vea.
                System.out.println("Resultado: " + resultado + "\n");
            } catch (ArithmeticException | IllegalArgumentException e) {
                // IllegalArgumentException hace falta porque Double.parseDouble
                // acepta los textos "NaN" e "Infinity" como numeros validos, y
                // la Calculadora los rechaza.
                System.out.println("Error: " + e.getMessage() + "\n");
            }
        }
        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("1. Sumar");
        System.out.println("2. Restar");
        System.out.println("3. Multiplicar");
        System.out.println("4. Dividir");
        System.out.println("5. Salir");
        System.out.print("Elige una opcion: ");
    }

    private static int leerOpcion(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double leerNumero(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero valido.");
            }
        }
    }
}
