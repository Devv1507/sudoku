package univalle.tedesoft.sudoku.models;

import java.util.*;

/**
 * Representa el tablero completo 6x6 de Sudoku.
 * Contiene la cuadrícula de celdas y gestiona los bloques (Block) que la componen.
 * Se encarga de la inicialización del tablero con un estado válido.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 */
public class Board {
    /**
     * Dimensión de la cuadrícula (6x6).
     */
    public static final int GRID_SIZE = 6;
    /**
     * Número de filas en un bloque.
     */
    public static final int BLOCK_ROWS = Block.BLOCK_ROWS;
    /**
     * Número de columnas en un bloque.
     */
    public static final int BLOCK_COLS = Block.BLOCK_COLS;
    /**
     * Número de filas de bloques en el tablero.
     */
    public static final int NUM_BLOCK_ROWS = GRID_SIZE / BLOCK_ROWS;
    /**
     * Número de columnas de bloques en el tablero.
     */
    public static final int NUM_BLOCK_COLS = GRID_SIZE / BLOCK_COLS;
    /**
     * Número deseado de celdas fijas (no editables) por bloque.
     */
    private static final int FIXED_CELLS_PER_BLOCK = 2;

    /**
     * Cuadrícula de celdas que representa el tablero de Sudoku.
     * Cada celda puede ser editable o no, y contiene un valor (0 si está vacía).
     * @see Cell
     */
    private final Cell[][] grid;
    /**
     * Estado inicial del tablero, usado para guardar la solución generada.
     * Cada celda tiene un valor y un estado de edición (editable o no).
     * @see Cell
     */
    private final Cell[][] initialGridState;
    /**
     * Matriz de bloques que componen el tablero.
     * Cada bloque es una sección 2x3 del tablero principal.
     * @see Block
     */
    private final Block[][] blocks;

    /**
     * Constructor del tablero. Inicializa la cuadrícula de celdas vacías y los bloques.
     */
    public Board() {
        // Arreglos donde se alojarán las celdas del sudoku
        this.grid = new Cell[GRID_SIZE][GRID_SIZE];
        this.initialGridState = new Cell[GRID_SIZE][GRID_SIZE];
        this.blocks = new Block[NUM_BLOCK_ROWS][NUM_BLOCK_COLS];

        // Inicializar celdas vacías
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = new Cell();
                initialGridState[i][j] = new Cell();
            }
        }

        // Crear las instancias de Block, pasando la cuadrícula principal y las coordenadas de inicio
        for (int blockRow = 0; blockRow < NUM_BLOCK_ROWS; blockRow++) {
            for (int blockColumn = 0; blockColumn < NUM_BLOCK_COLS; blockColumn++) {
                int startRow = blockRow * BLOCK_ROWS;
                int startCol = blockColumn * BLOCK_COLS;
                blocks[blockRow][blockColumn] = new Block(grid, startRow, startCol);
            }
        }
    }

    /**
     * Inicializa el tablero con un patrón de Sudoku 6x6 válido y parcialmente lleno.
     * Genera una solución, la guarda, y luego crea el puzzle con celdas fijas.
     */
    public void initializeBoard() {
        this.clearBoard();
        if (!this.generateSolution(0, 0)) {
            System.err.println("Error: No se pudo generar una solución de Sudoku válida.");
            return;
        }
        this.saveInitialState(); // Guardar la solución generada
        this.makePuzzle(); // Crear los "agujeros" y definir celdas fijas
        System.out.println("Tablero inicializado:");
        this.printBoard();
    }

    /**
     * Limpia completamente el tablero, reiniciando todas las celdas a vacías y editables.
     */
    private void clearBoard() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.grid[i][j].setEditable(true);
                this.grid[i][j].setValue(0);
            }
        }
    }

    /**
     * Intenta generar una solución completa para el tablero usando backtracking.
     * (Implementación sin cambios respecto a la versión anterior)
     * @param row Fila actual.
     * @param col Columna actual.
     * @return true si se encontró una solución, false en caso contrario.
     */
    private boolean generateSolution(int row, int col) {
        if (col == GRID_SIZE) {
            col = 0;
            row++;
            if (row == GRID_SIZE) {
                return true;
            }
        }
        if (this.grid[row][col].getValue() != 0) {
            return generateSolution(row, col + 1);
        }

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= GRID_SIZE; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        for (int num = 1; num < (numbers.size()+1); num++) {
            if (isValidPlacementForGeneration(row, col, num)) {
                this.grid[row][col].setValue(num);
                if (generateSolution(row, col + 1)) {
                    return true;
                }
                this.grid[row][col].setValue(0); // Backtrack
            }
        }
        return false;
    }

     /**
     * Verifica si colocar un número durante la *generación* de la solución es válido.
     * Es idéntico a isValidPlacement pero opera sobre `grid` directamente.
     * @param row Fila.
     * @param col Columna.
     * @param num Número a probar.
     * @return true si es válido colocarlo.
     */
    private boolean isValidPlacementForGeneration(int row, int col, int num) {
        // Comprobar fila
        for (int c = 0; c < GRID_SIZE; c++) {
            if (grid[row][c].getValue() == num) return false;
        }
        // Comprobar columna
        for (int r = 0; r < GRID_SIZE; r++) {
            if (grid[r][col].getValue() == num) return false;
        }
        // Comprobar bloque
        int blockStartRow = row - row % BLOCK_ROWS;
        int blockStartCol = col - col % BLOCK_COLS;
        for (int r = 0; r < BLOCK_ROWS; r++) {
            for (int c = 0; c < BLOCK_COLS; c++) {
                if (grid[blockStartRow + r][blockStartCol + c].getValue() == num) {
                    return false;
                }
            }
        }
        return true;
    }

     /**
      * Verifica si la colocación de un número por parte del *usuario* es válida
      * en el estado *actual* del tablero (puede tener celdas vacías).
      * @param row Fila de la celda.
      * @param col Columna de la celda.
      * @param num Número a colocar (1-6).
      * @return true si la colocación no viola las reglas inmediatamente, false en caso contrario.
      */
    public boolean isValidPlacement(int row, int col, int num) {
        if (num == 0) {
            return true; // Vaciar siempre es válido en sí mismo
        }

        // Comprobar fila (ignorando la propia celda)
        for (int c = 0; c < GRID_SIZE; c++) {
            if (c != col && grid[row][c].getValue() == num) {
                return false;
            }
        }
        // Comprobar columna (ignorando la propia celda)
        for (int r = 0; r < GRID_SIZE; r++) {
            if (r != row && grid[r][col].getValue() == num) {
                return false;
            }
        }
        // Comprobar bloque (ignorando la propia celda)
        int blockStartRow = row - row % BLOCK_ROWS;
        int blockStartCol = col - col % BLOCK_COLS;
        for (int r = 0; r < BLOCK_ROWS; r++) {
            for (int c = 0; c < BLOCK_COLS; c++) {
                int checkRow = blockStartRow + r;
                int checkCol = blockStartCol + c;
                if ((checkRow != row || checkCol != col) && grid[checkRow][checkCol].getValue() == num) {
                    return false;
                }
            }
        }
        return true; // Es válido
    }


    /**
     * Guarda el estado actual de la cuadrícula `grid` (la solución completa) en `initialGridState`.
     */
    private void saveInitialState() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.initialGridState[i][j].setValue(grid[i][j].getValue());
                this.initialGridState[i][j].setEditable(false); // Marcar como no editable en la referencia
            }
        }
    }

    /**
     * Modifica la cuadrícula `grid` (que contiene la solución) para crear el puzzle.
     * Selecciona celdas para que sean fijas (no editables) y vacía las demás (editables).
     * Intenta seguir la regla de `FIXED_CELLS_PER_BLOCK` celdas fijas por bloque.
     * (Implementación sin cambios respecto a la versión anterior, ya usaba lógica de bloques implícita).
     */
    private void makePuzzle() {
         List<int[]> allCells = new ArrayList<>();
         for (int r = 0; r < GRID_SIZE; r++) {
             for (int c = 0; c < GRID_SIZE; c++) {
                 allCells.add(new int[]{r, c});
             }
         }
         Collections.shuffle(allCells);

         int fixedCellsCount = 0;
         int targetFixedCells = NUM_BLOCK_ROWS * NUM_BLOCK_COLS * FIXED_CELLS_PER_BLOCK;
         boolean[][] isFixed = new boolean[GRID_SIZE][GRID_SIZE];
         int[] fixedInBlock = new int[NUM_BLOCK_ROWS * NUM_BLOCK_COLS];

         for (int[] cellCoord : allCells) {
             if (fixedCellsCount >= targetFixedCells) break;
             int row = cellCoord[0];
             int col = cellCoord[1];
             int blockIndex = (row / BLOCK_ROWS) * NUM_BLOCK_COLS + (col / BLOCK_COLS);

             if (fixedInBlock[blockIndex] < FIXED_CELLS_PER_BLOCK) {
                 isFixed[row][col] = true;
                 fixedInBlock[blockIndex]++;
                 fixedCellsCount++;
             }
         }

         for (int row = 0; row < GRID_SIZE; row++) {
             for (int col = 0; col < GRID_SIZE; col++) {
                 if (isFixed[row][col]) {
                     this.grid[row][col].setValue(this.initialGridState[row][col].getValue()); // Restaurar valor de la solución
                     this.grid[row][col].setEditable(false);                         // Marcar como no editable
                 } else {
                     this.grid[row][col].setValue(0);     // Vaciar celda
                     this.grid[row][col].setEditable(true); // Marcar como editable
                 }
             }
         }
          if (fixedCellsCount < targetFixedCells) {
             System.err.println("Advertencia: No se pudieron fijar las " + targetFixedCells + " celdas deseadas. Faltaron " + (targetFixedCells - fixedCellsCount));
          }
    }

    /**
     * Obtiene la celda en la posición global especificada del tablero.
     *
     * @param row La fila global (0 a 5).
     * @param col La columna global (0 a 5).
     * @return La celda en esa posición.
     * @throws IndexOutOfBoundsException si los índices están fuera del rango 0-5.
     */
    public Cell getCell(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            throw new IndexOutOfBoundsException("Índices de celda fuera de rango: (" + row + ", " + col + ")");
        }
        return grid[row][col];
    }

    /**
     * Establece el valor de una celda en la posición global especificada, si es editable.
     *
     * @param row   La fila global (0 a 5).
     * @param col   La columna global (0 a 5).
     * @param value El valor a establecer (0 para vacío, 1-6).
     * @return true si el valor fue establecido, false si la celda no es editable.
     * @throws IndexOutOfBoundsException si los índices están fuera del rango.
     * @throws IllegalArgumentException si el valor no está entre 0 y 6.
     */
    public boolean setCellValue (int row, int col, int value) {
        if (value < 0 || value > GRID_SIZE) {
            throw new IllegalArgumentException("Valor inválido para celda: " + value);
        }
        Cell cell = this.getCell(row, col);
        boolean isCellEditable = cell.getEditable();
        if (isCellEditable) {
            cell.setValue(value);
            return true;
        }
        return false;
    }

    /**
     * Obtiene el bloque que contiene la celda en la posición global especificada.
     * @param row Fila global (0-5).
     * @param col Columna global (0-5).
     * @return El bloque correspondiente.
     * @throws IndexOutOfBoundsException si los índices están fuera de rango.
     */
    public Block getBlockAt(int row, int col) {
         if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            throw new IndexOutOfBoundsException("Índices de celda fuera de rango al buscar bloque: (" + row + ", " + col + ")");
        }
        int blockRow = row / BLOCK_ROWS;
        int blockCol = col / BLOCK_COLS;
        return this.blocks[blockRow][blockCol];
    }

    /**
     * Obtiene un bloque específico por sus índices de bloque.
     * @param blockRow Índice de fila del bloque (0 a NUM_BLOCK_ROWS - 1).
     * @param blockCol Índice de columna del bloque (0 a NUM_BLOCK_COLS - 1).
     * @return El bloque solicitado.
     * @throws IndexOutOfBoundsException si los índices de bloque están fuera de rango.
     */
    public Block getBlock(int blockRow, int blockCol) {
         if (blockRow < 0 || blockRow >= NUM_BLOCK_ROWS || blockCol < 0 || blockCol >= NUM_BLOCK_COLS) {
            throw new IndexOutOfBoundsException("Índices de bloque fuera de rango: (" + blockRow + ", " + blockCol + ")");
        }
        return blocks[blockRow][blockCol];
    }

    /**
     * Imprime el tablero actual en la consola (para depuración).
     * Muestra 'X' para celdas vacías y marca las fijas con '*'.
     * Incluye separadores de bloque.
     * BORRAR LUEGO!!! ************************************************************************************************
     */
    public void printBoard() {
        System.out.println("-------------------------");
        for (int i = 0; i < GRID_SIZE; i++) {
            System.out.print("| ");
            for (int j = 0; j < GRID_SIZE; j++) {
                Cell cell = grid[i][j];
                char editableMarker = cell.getEditable() ? ' ' : '*';
                String valueStr = cell.getValue() == 0 ? " " : String.valueOf(cell.getValue()); // Espacio para vacío
                System.out.print(valueStr + editableMarker);
                if ((j + 1) % BLOCK_COLS == 0) { // Si es fin de columna de bloque
                    System.out.print(" | ");
                } else {
                    System.out.print(" "); // Espacio entre celdas normales
                }
            }
            System.out.println();
            if ((i + 1) % BLOCK_ROWS == 0 && i < GRID_SIZE - 1) { // Si es fin de fila de bloque
                System.out.println("-------------------------"); // Separador horizontal
            }
        }
         System.out.println("-------------------------");
    }

    /**
     * Obtiene una copia de la cuadrícula actual del tablero.
     * @return Una nueva matriz 2D de celdas con los valores y estados de edición actuales.
     */
    public Cell[][] getGridSnapshot() {
        Cell[][] snapshot = new Cell[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                // Crear nueva instancia de Cell para la copia profunda
                snapshot[i][j] = new Cell(grid[i][j].getValue(), grid[i][j].getEditable());
            }
        }
        return snapshot;
    }
}
