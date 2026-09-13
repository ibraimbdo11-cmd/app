import { QuestionRequirements } from '../../types';

/**
 * QuestionUnderstanding analyzes Arabic and English programming questions
 * and extracts structured parameters, function names, return criteria, and constraints.
 */
export class QuestionUnderstanding {
  public static analyze(questionText: string): QuestionRequirements {
    const text = questionText.trim();
    const isArabic = /[\u0600-\u06FF]/.test(text);

    const functionName = this.extractFunctionName(text, isArabic);
    const parameters = this.extractParameters(text, isArabic);
    const returnDescription = this.extractReturnDescription(text, isArabic);

    const requiresArrayOperations =
      /مصفوفة|عناصر|array|list|elements|slice|map|filter|reduce/i.test(text);

    const requiresLoop =
      /حلقة|تكرار|loop|iterate|for\s*loop|while/i.test(text) &&
      !/بدون استخدام|بدون تكرار|without.*loop|no.*loop|don't use.*loop/i.test(text);

    const forbidsLoop =
      /بدون استخدام حلقات|بدون تكرار|دون استخدام loop|without using.*loop|no loop|without loops/i.test(
        text
      );

    const requiresObject =
      /كائن|خصائص|object|properties|key-value|dictionary/i.test(text);

    return {
      functionName,
      parameters,
      returnDescription,
      requiresArrayOperations,
      requiresLoop,
      forbidsLoop,
      requiresObject,
      detectedLanguage: isArabic ? 'ar' : 'en',
    };
  }

  private static extractFunctionName(text: string, isArabic: boolean): string {
    // 1. Explicit quotes or code blocks: `functionName` or 'functionName'
    const backtickMatch = text.match(/`([a-zA-Z_$][a-zA-Z0-9_$]*)`/);
    if (backtickMatch) return backtickMatch[1];

    // 2. Arabic patterns: باسم X, تسمى X, تدعى X
    if (isArabic) {
      const arNamedMatch = text.match(/(?:باسم|تسمى|تدعى|اسمها)\s+([a-zA-Z_$][a-zA-Z0-9_$]*)/i);
      if (arNamedMatch) return arNamedMatch[1];

      const arFuncMatch = text.match(/دالة\s+([a-zA-Z_$][a-zA-Z0-9_$]*)/i);
      if (arFuncMatch && !['تقبل', 'ترجع', 'تأخذ', 'جديدة'].includes(arFuncMatch[1])) {
        return arFuncMatch[1];
      }
    }

    // 3. English patterns: named X, called X, function X, function named X
    const enNamedMatch = text.match(
      /(?:named|called|function\s+named|function)\s+([a-zA-Z_$][a-zA-Z0-9_$]*)/i
    );
    if (
      enNamedMatch &&
      !['that', 'which', 'to', 'the', 'a', 'an', 'and', 'with', 'takes', 'returns'].includes(
        enNamedMatch[1].toLowerCase()
      )
    ) {
      return enNamedMatch[1];
    }

    // 4. Function call signature: add(a, b), square(x), isEven(num)
    const sigMatch = text.match(/([a-zA-Z_$][a-zA-Z0-9_$]*)\s*\(([^)]*)\)/);
    if (
      sigMatch &&
      !['function', 'if', 'for', 'while', 'switch', 'console', 'return'].includes(sigMatch[1])
    ) {
      return sigMatch[1];
    }

    // 5. Semantic keyword inferences
    if (/جمع|مجموع|add|sum/i.test(text)) return 'add';
    if (/مربع|تربيع|square/i.test(text)) return 'square';
    if (/ضرب|جداء|multiply|product/i.test(text)) return 'multiply';
    if (/زوجي|فردي|isEven|even/i.test(text)) return 'isEven';
    if (/عكس|مقلوب|reverse/i.test(text)) return 'reverseString';
    if (/أكبر|اقصى|max|maximum/i.test(text)) return 'findMax';

    return 'solution';
  }

  private static extractParameters(text: string, isArabic: boolean): string[] {
    // Check for signature parentheses: func(a, b) or (num)
    const sigMatch = text.match(/[a-zA-Z_$][a-zA-Z0-9_$]*\s*\(([^)]+)\)/);
    if (sigMatch) {
      const rawParams = sigMatch[1]
        .split(',')
        .map(p => p.trim())
        .filter(p => /^[a-zA-Z_$][a-zA-Z0-9_$]*$/.test(p));
      if (rawParams.length > 0) return rawParams;
    }

    // Parameter counts in text
    if (isArabic) {
      if (/معاملين|معاملان|عددين|رقمين|متغيرين/i.test(text)) return ['a', 'b'];
      if (/ثلاثة أرقام|ثلاثة أعداد|ثلاث معاملات/i.test(text)) return ['a', 'b', 'c'];
      if (/معامل واحد|رقم واحد|عدد واحد|مصفوفة/i.test(text)) return ['x'];
    } else {
      if (/two numbers|two arguments|two parameters|two values/i.test(text)) return ['a', 'b'];
      if (/three numbers|three arguments|three parameters/i.test(text)) return ['a', 'b', 'c'];
      if (/single number|one number|an array|a string/i.test(text)) return ['x'];
    }

    // Default to two params for binary math or single for unary
    if (/جمع|ضرب|add|multiply|sum|max/i.test(text)) return ['a', 'b'];
    if (/مربع|زوجي|square|even|odd|reverse/i.test(text)) return ['x'];

    return ['a', 'b'];
  }

  private static extractReturnDescription(text: string, isArabic: boolean): string {
    if (isArabic) {
      const returnMatch = text.match(/(?:ترجع|تعيد|تُرجع|إرجاع|ناتج)\s+([^.\n]+)/);
      if (returnMatch) return returnMatch[1].trim();
    } else {
      const returnMatch = text.match(/(?:returns?|should return|output|yielding)\s+([^.\n]+)/i);
      if (returnMatch) return returnMatch[1].trim();
    }
    return 'computed result';
  }
}
