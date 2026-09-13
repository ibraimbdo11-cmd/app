import { QuestionRequirements, SolveResult } from '../../types';
import { JavaScriptSolver } from './JavaScriptSolver';
import { JavaScriptValidator } from './JavaScriptValidator';

export class JavaScriptRepairLoop {
  private static readonly MAX_ATTEMPTS = 3;

  public static async executeWithRepair(
    requirements: QuestionRequirements,
    questionText: string
  ): Promise<SolveResult> {
    const errors: string[] = [];
    let currentCode = '';
    let currentPrompt = questionText;
    let attempts = 0;
    let lastSolverUsed: 'gemini' | 'local_rules' = 'local_rules';

    while (attempts < this.MAX_ATTEMPTS) {
      attempts++;

      const solveResult = await JavaScriptSolver.solve(requirements, currentPrompt);
      currentCode = solveResult.code;
      lastSolverUsed = solveResult.solverUsed;

      const validation = JavaScriptValidator.validate(currentCode, requirements);

      if (validation.isValid) {
        return {
          success: true,
          code: currentCode,
          explanation: solveResult.explanation,
          attemptCount: attempts,
          errors,
          solverUsed: lastSolverUsed,
        };
      }

      // Record error
      const err = validation.errorMessage || 'Unknown validation failure';
      errors.push(`Attempt ${attempts}: ${err}`);

      // If local deterministic solution is available and first attempt failed with API, fallback
      if (lastSolverUsed === 'gemini') {
        currentCode = JavaScriptSolver.solveLocally(requirements, questionText);
        const localVal = JavaScriptValidator.validate(currentCode, requirements);
        if (localVal.isValid) {
          return {
            success: true,
            code: currentCode,
            explanation: 'Recovered via JS Agent Deterministic Engine',
            attemptCount: attempts + 1,
            errors,
            solverUsed: 'local_rules',
          };
        }
      }

      // Enrich prompt with repair feedback
      currentPrompt = `${questionText}\n\n[REPAIR REQUIRED - Attempt ${attempts}]:\nPrevious code failed validation: ${err}.\nHint: ${validation.feedbackHint || 'Correct syntax and function definition'}.\nPlease output the corrected JavaScript code.`;
    }

    // Return best attempt even if not 100% validated
    return {
      success: false,
      code: currentCode,
      explanation: 'Exceeded max repair attempts',
      attemptCount: attempts,
      errors,
      solverUsed: lastSolverUsed,
    };
  }
}
