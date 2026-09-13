import { JavaScriptQuestion } from '../types';

export const DEMO_CHALLENGES: JavaScriptQuestion[] = [
  {
    id: 'challenge-add',
    title: 'دالة الجمع (Add Function)',
    titleArabic: 'دالة الجمع',
    prompt:
      'اكتب دالة JavaScript باسم `add` تقبل معاملين `(a, b)` وترجع حاصل جمعهما (`a + b`).',
    initialCode: 'function add(a, b) {\n  // اكتب كود الجمع هنا\n}',
    expectedOutputHint: 'add(2, 3) === 5',
    testCases: [
      { args: [2, 3], expected: 5, label: 'add(2, 3)' },
      { args: [-1, 5], expected: 4, label: 'add(-1, 5)' },
      { args: [10, 20], expected: 30, label: 'add(10, 20)' },
    ],
  },
  {
    id: 'challenge-square',
    title: 'دالة المربع (Square Function)',
    titleArabic: 'دالة المربع',
    prompt:
      'اكتب دالة JavaScript تسمى `square` تقبل رقماً واحداً `x` وتعيد مربع هذا الرقم (`x * x`).',
    initialCode: 'function square(x) {\n  // اكتب الحل هنا\n}',
    expectedOutputHint: 'square(4) === 16',
    testCases: [
      { args: [4], expected: 16, label: 'square(4)' },
      { args: [0], expected: 0, label: 'square(0)' },
      { args: [-5], expected: 25, label: 'square(-5)' },
    ],
  },
  {
    id: 'challenge-multiply',
    title: 'Multiplication (English)',
    titleArabic: 'دالة الضرب',
    prompt:
      'Write a JavaScript function named `multiply` that takes two parameters `(a, b)` and returns their product (`a * b`).',
    initialCode: 'function multiply(a, b) {\n  // Write multiplication logic\n}',
    expectedOutputHint: 'multiply(3, 4) === 12',
    testCases: [
      { args: [3, 4], expected: 12, label: 'multiply(3, 4)' },
      { args: [7, 0], expected: 0, label: 'multiply(7, 0)' },
      { args: [-2, 5], expected: -10, label: 'multiply(-2, 5)' },
    ],
  },
  {
    id: 'challenge-is-even',
    title: 'التحقق من الأعداد الزوجية (Even Check)',
    titleArabic: 'التحقق من الأعداد الزوجية',
    prompt:
      'اكتب دالة باسم `isEven` تقبل عدداً `num` وترجع `true` إذا كان العدد زوجياً و `false` إذا كان فردياً.',
    initialCode: 'function isEven(num) {\n  // تحقق من العدد الزوجي\n}',
    expectedOutputHint: 'isEven(4) === true, isEven(7) === false',
    testCases: [
      { args: [4], expected: true, label: 'isEven(4)' },
      { args: [7], expected: false, label: 'isEven(7)' },
      { args: [0], expected: true, label: 'isEven(0)' },
    ],
  },
];
