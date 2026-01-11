package savethesystem.logic;

public class Problem {
    private String title;
    private int stabilityImpact;
    private int performanceImpact;
    private int securityImpact;

    public Problem(String title, int stab, int perf, int sec) {
        this.title = title;
        this.stabilityImpact = stab;
        this.performanceImpact = perf;
        this.securityImpact = sec;
    }

    public String getTitle() { return title; }
    public int getStabilityImpact() { return stabilityImpact; }
    public int getPerformanceImpact() { return performanceImpact; }
    public int getSecurityImpact() { return securityImpact; }
}