package rs.ac.kg.fin.albus.hagrid.data.container;


public record CodeExecutionResult(String output,
                                  String error) {

    public boolean hasError() {
        return error != null && !error.isBlank();
    }
}
