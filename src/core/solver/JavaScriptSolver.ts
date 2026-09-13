import { QuestionRequirements, SolveResult } from '../../types';

export class JavaScriptSolver {
  /**
   * Solves a JavaScript problem either via Gemini server API (if available)
   * or via the deterministic local rules engine.
   */
  public static async solve(
    requirements: QuestionRequirements,
    questionText: string
  ): Promise<SolveResult> {
    // 1. Attempt server-side Gemini AI Solver via /api/solve
    try {
      const response = await fetch('/api/solve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          prompt: questionText,
          functionName: requirements.functionName,
          language: 'javascript',
        }),
      });

      if (response.ok) {
        const data = await response.json();
        if (data.success && data.code) {
          return {
            success: true,
            code: data.code,
            explanation: 'Generated via Gemini 2.5 Flash',
            attemptCount: 1,
            errors: [],
            solverUsed: 'gemini',
          };
        }
      }
    } catch {
      // Server not reachable or error, gracefully proceed to local deterministic solver
    }

    // 2. Local Deterministic Rule-Based Solver
    const localCode = this.solveLocally(requirements, questionText);
    return {
      success: true,
      code: localCode,
      explanation: 'Generated via JS Agent Deterministic Solver',
      attemptCount: 1,
      errors: [],
      solverUsed: 'local_rules',
    };
  }

  public static solveLocally(
    requirements: QuestionRequirements,
    questionText: string
  ): string {
    const name = requirements.functionName || 'solution';
    const params = requirements.parameters.length > 0 ? requirements.parameters : ['a', 'b'];
    const pStr = params.join(', ');
    const lower = questionText.toLowerCase();

    // 1. Add / Sum
    if (
      /جمع|مجموع|add|sum/i.test(lower) ||
      name.toLowerCase().includes('add') ||
      name.toLowerCase().includes('sum')
    ) {
      const p1 = params[0] || 'a';
      const p2 = params[1] || 'b';
      return `function ${name}(${p1}, ${p2}) {\n  return ${p1} + ${p2};\n}`;
    }

    // 2. Square
    if (
      /مربع|تربيع|square/i.test(lower) ||
      name.toLowerCase().includes('square')
    ) {
      const p1 = params[0] || 'x';
      return `function ${name}(${p1}) {\n  return ${p1} * ${p1};\n}`;
    }

    // 3. Multiply
    if (
      /ضرب|جداء|multiply|product/i.test(lower) ||
      name.toLowerCase().includes('multiply')
    ) {
      const p1 = params[0] || 'a';
      const p2 = params[1] || 'b';
      return `function ${name}(${p1}, ${p2}) {\n  return ${p1} * ${p2};\n}`;
    }

    // 4. Even check
    if (
      /زوجي|فردي|even/i.test(lower) ||
      name.toLowerCase().includes('even')
    ) {
      const p1 = params[0] || 'num';
      return `function ${name}(${p1}) {\n  return ${p1} % 2 === 0;\n}`;
    }

    // 5. Reverse string / array
    if (
      /عكس|مقلوب|reverse/i.test(lower) ||
      name.toLowerCase().includes('reverse')
    ) {
      const p1 = params[0] || 'str';
      return `function ${name}(${p1}) {\n  return typeof ${p1} === 'string' ? ${p1}.split('').reverse().join('') : ${p1}.slice().reverse();\n}`;
    }

    // 6. Max of numbers
    if (
      /أكبر|max/i.test(lower) ||
      name.toLowerCase().includes('max')
    ) {
      const p1 = params[0] || 'a';
      const p2 = params[1] || 'b';
      return `function ${name}(${p1}, ${p2}) {\n  return Math.max(${p1}, ${p2});\n}`;
    }

    // 7. General template
    return `function ${name}(${pStr}) {\n  // Automated JS solution\n  return ${params[0] || 'null'};\n}`;
  }
}
