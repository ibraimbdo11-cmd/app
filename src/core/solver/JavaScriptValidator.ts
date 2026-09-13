import { QuestionRequirements, ValidationResult } from '../../types';

export class JavaScriptValidator {
  public static validate(
    code: string,
    requirements: QuestionRequirements
  ): ValidationResult {
    const trimmed = code.trim();

    if (!trimmed) {
      return {
        isValid: false,
        errorMessage: 'Empty code submission',
        errorCategory: 'SYNTAX',
        feedbackHint: 'Please write a JavaScript function implementation.',
      };
    }

    // 1. Language purity verification (Anti-Python / Anti-Java etc.)
    const purityCheck = this.checkLanguagePurity(trimmed);
    if (!purityCheck.isValid) {
      return purityCheck;
    }

    // 2. Bracket and parenthesis balancing
    const bracketCheck = this.checkBalancedTokens(trimmed);
    if (!bracketCheck.isValid) {
      return bracketCheck;
    }

    // 3. String quotes balancing
    const quotesCheck = this.checkBalancedQuotes(trimmed);
    if (!quotesCheck.isValid) {
      return quotesCheck;
    }

    // 4. Function name presence
    if (requirements.functionName && requirements.functionName !== 'solution') {
      const funcNameRegex = new RegExp(
        `(?:function\\s+${requirements.functionName}\\b|(?:const|let|var)\\s+${requirements.functionName}\\s*=)`
      );
      if (!funcNameRegex.test(trimmed)) {
        return {
          isValid: false,
          errorMessage: `Expected function name '${requirements.functionName}' was not found in code`,
          errorCategory: 'SIGNATURE',
          feedbackHint: `Define the function with name '${requirements.functionName}': function ${requirements.functionName}(...)`,
        };
      }
    }

    // 5. Return statement requirement
    if (!/return\b/.test(trimmed) && !/=>\s*[^{]/.test(trimmed)) {
      return {
        isValid: false,
        errorMessage: 'Function is missing a return statement',
        errorCategory: 'SEMANTICS',
        feedbackHint: 'Ensure your function returns the computed result using the `return` keyword.',
      };
    }

    // 6. Constraint checks (e.g. forbidden loops)
    if (requirements.forbidsLoop) {
      if (/\b(for|while|do)\b/.test(trimmed)) {
        return {
          isValid: false,
          errorMessage: 'Detected loop keyword (for/while/do), but problem forbids loops',
          errorCategory: 'CONSTRAINTS',
          feedbackHint: 'Solve the problem without using for or while loops (e.g. direct formula or recursion).',
        };
      }
    }

    return { isValid: true };
  }

  private static checkLanguagePurity(code: string): ValidationResult {
    // Python detection
    if (/\bdef\s+[a-zA-Z_]/.test(code)) {
      return {
        isValid: false,
        errorMessage: 'Detected Python syntax (`def`). Only JavaScript is allowed.',
        errorCategory: 'LANGUAGE_PURITY',
        feedbackHint: 'Use `function name() {}` or arrow syntax instead of Python `def`.',
      };
    }
    if (/\belif\b/.test(code)) {
      return {
        isValid: false,
        errorMessage: 'Detected Python `elif`. Use `else if` in JavaScript.',
        errorCategory: 'LANGUAGE_PURITY',
      };
    }
    if (/\b(None|True|False)\b/.test(code)) {
      return {
        isValid: false,
        errorMessage: 'Detected Python literals (None/True/False). Use null/true/false in JavaScript.',
        errorCategory: 'LANGUAGE_PURITY',
      };
    }
    if (/\bprint\s*\(/.test(code) && !/console\.log/.test(code)) {
      return {
        isValid: false,
        errorMessage: 'Detected `print()` call. Use `return` or `console.log()` in JavaScript.',
        errorCategory: 'LANGUAGE_PURITY',
      };
    }

    // Java / C++ detection
    if (/\bpublic\s+(static\s+)?(class|void|int|String)\b/.test(code)) {
      return {
        isValid: false,
        errorMessage: 'Detected Java/C# class or typed method structure. Only pure JavaScript is supported.',
        errorCategory: 'LANGUAGE_PURITY',
      };
    }
    if (/#include\s*<|std::/.test(code)) {
      return {
        isValid: false,
        errorMessage: 'Detected C++ syntax. Only pure JavaScript is supported.',
        errorCategory: 'LANGUAGE_PURITY',
      };
    }
    if (/<\?php/.test(code)) {
      return {
        isValid: false,
        errorMessage: 'Detected PHP code. Only pure JavaScript is supported.',
        errorCategory: 'LANGUAGE_PURITY',
      };
    }

    return { isValid: true };
  }

  private static checkBalancedTokens(code: string): ValidationResult {
    const stack: string[] = [];
    let inSingleQuote = false;
    let inDoubleQuote = false;
    let inBacktick = false;
    let inLineComment = false;
    let inBlockComment = false;

    for (let i = 0; i < code.length; i++) {
      const c = code[i];
      const next = i + 1 < code.length ? code[i + 1] : '';
      const prev = i > 0 ? code[i - 1] : '';

      // Handle comments
      if (inLineComment) {
        if (c === '\n') inLineComment = false;
        continue;
      }
      if (inBlockComment) {
        if (prev === '*' && c === '/') inBlockComment = false;
        continue;
      }

      if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
        if (c === '/' && next === '/') {
          inLineComment = true;
          i++;
          continue;
        }
        if (c === '/' && next === '*') {
          inBlockComment = true;
          i++;
          continue;
        }
      }

      // Handle quotes
      if (c === "'" && !inDoubleQuote && !inBacktick && prev !== '\\') {
        inSingleQuote = !inSingleQuote;
        continue;
      }
      if (c === '"' && !inSingleQuote && !inBacktick && prev !== '\\') {
        inDoubleQuote = !inDoubleQuote;
        continue;
      }
      if (c === '`' && !inSingleQuote && !inDoubleQuote && prev !== '\\') {
        inBacktick = !inBacktick;
        continue;
      }

      if (inSingleQuote || inDoubleQuote || inBacktick) {
        continue;
      }

      // Check brackets
      if (c === '{' || c === '(' || c === '[') {
        stack.push(c);
      } else if (c === '}' || c === ')' || c === ']') {
        if (stack.length === 0) {
          return {
            isValid: false,
            errorMessage: `Unmatched closing bracket '${c}'`,
            errorCategory: 'SYNTAX',
            feedbackHint: `Check token balance; '${c}' has no opening counterpart.`,
          };
        }
        const top = stack.pop();
        if (
          (c === '}' && top !== '{') ||
          (c === ')' && top !== '(') ||
          (c === ']' && top !== '[')
        ) {
          return {
            isValid: false,
            errorMessage: `Mismatched brackets: expected matching closing for '${top}' but found '${c}'`,
            errorCategory: 'SYNTAX',
          };
        }
      }
    }

    if (stack.length > 0) {
      const unclosed = stack.pop();
      return {
        isValid: false,
        errorMessage: `Unclosed bracket '${unclosed}'`,
        errorCategory: 'SYNTAX',
        feedbackHint: `Check for unclosed '${unclosed}'.`,
      };
    }

    return { isValid: true };
  }

  private static checkBalancedQuotes(code: string): ValidationResult {
    let singleQuotes = 0;
    let doubleQuotes = 0;
    let backticks = 0;

    for (let i = 0; i < code.length; i++) {
      const c = code[i];
      const prev = i > 0 ? code[i - 1] : '';
      if (prev === '\\') continue;

      if (c === "'") singleQuotes++;
      if (c === '"') doubleQuotes++;
      if (c === '`') backticks++;
    }

    if (singleQuotes % 2 !== 0) {
      return {
        isValid: false,
        errorMessage: "Unmatched single quote (') in code string",
        errorCategory: 'SYNTAX',
      };
    }
    if (doubleQuotes % 2 !== 0) {
      return {
        isValid: false,
        errorMessage: 'Unmatched double quote (") in code string',
        errorCategory: 'SYNTAX',
      };
    }
    if (backticks % 2 !== 0) {
      return {
        isValid: false,
        errorMessage: 'Unmatched backtick (`) in template literal',
        errorCategory: 'SYNTAX',
      };
    }

    return { isValid: true };
  }
}
