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

            if (opcion == 8) {
                salir = true;
                System.out.println("Hasta luego!");
                continue;
            }
            if (opcion < 1 || opcion > 8) {
                System.out.println("Opcion invalida. Intenta de nuevo.\n");
                continue;
            }

            try {
                double resultado;
                if (opcion == 6) {
                    double x = leerNumero(scanner, "Ingresa el numero: ");
                    resultado = calculadora.raizCuadrada(x);
                } else {
                    double a = leerNumero(scanner, "Ingresa el primer numero: ");
                    double b = leerNumero(scanner, "Ingresa el segundo numero: ");
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
                        case 5:
                            resultado = calculadora.potencia(a, b);
                            break;
                        case 7:
                            resultado = calculadora.modulo(a, b);
                            break;
                        default:
                            resultado = 0.0;
                            break;
                    }
                }
                System.out.println("Resultado: " + resultado + "\n");
            } catch (ArithmeticException | IllegalArgumentException e) {
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
        System.out.println("5. Potencia");
        System.out.println("6. Raiz cuadrada");
        System.out.println("7. Modulo");
        System.out.println("8. Salir");
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
