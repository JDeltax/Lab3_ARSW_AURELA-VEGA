package edu.eci.arsw.blueprints.dto;

public class ApiResponse<T>{

    private int status;       // Código HTTP (ej. 200, 400, 404, 500)
    private String message;   // Mensaje descriptivo para el cliente
    private T data;           // El cuerpo de la respuesta (puede ser un objeto, lista, etc.)
    private String error;


    //CONSTRUCTOR DE SWUCCESS
    public ApiResponse(T data, String message, int status) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.error = null;
    }

    //CONSTRUCTOR DE ERROR
    public ApiResponse(String error, String message, int status) {
        this.status = status;
        this.message = message;
        this.data = null;
        this.error = error;
    }


    // MEtodos de Fabrica para crear respuestas de éxito y error
    public static <T> ApiResponse<T> success(T data, int status, String message) {
        
        return new ApiResponse<>(data, message, status);
    }

    public static <T> ApiResponse<T> error(String error, int status, String message) {
        return new ApiResponse<>(error, message, status);
    }
    //GETTERS Y SETTERS
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    // COnstructores 
}
