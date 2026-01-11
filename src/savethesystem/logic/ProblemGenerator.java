package savethesystem.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProblemGenerator {

    private List<Problem> problems = new ArrayList<>();
    private Random random = new Random();

    public ProblemGenerator() {
        problems.add(new Problem("Server overload detected!", -15, -10, -5));
        problems.add(new Problem("Database connection lost!", -10, -20, -5));
        problems.add(new Problem("Suspicious login detected!", -5, -5, -20));
        problems.add(new Problem("Memory leak detected!", -15, -15, -5));
        problems.add(new Problem("CPU usage critical!", -10, -20, -10));
        problems.add(new Problem("Network latency high!", -10, -15, -5));
    }

    public Problem getRandomProblem() {
        return problems.get(random.nextInt(problems.size()));
    }
}