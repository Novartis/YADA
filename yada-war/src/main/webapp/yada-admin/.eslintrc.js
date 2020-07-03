// https://eslint.org/docs/user-guide/configuring

module.exports = {

  root: true,
  parserOptions: {
    parser: 'babel-eslint'
  },
  env: {
    browser: true,
  },
  extends: [
    // https://github.com/vuejs/eslint-plugin-vue#priority-a-essential-error-prevention
    // consider switching to `plugin:vue/strongly-recommended` or `plugin:vue/recommended` for stricter rules.
    // or 'plugin:vue/essential' for more leniency
    'plugin:vue/strongly-recommended',
    // https://github.com/standard/standard/blob/master/docs/RULES-en.md
    'standard'
  ],
  globals: {
    "$": true,
    "jQuery": true
  },
  // required to lint *.vue files
  plugins: [
    'vue',
    // https://github.com/jrsearles/eslint-plugin-brace-rules
    // help:  https://github.com/jrsearles/eslint-plugin-brace-rules/issues/2
    'brace-rules'
  ],
  // add your custom rules here
  rules: {

    'curly': [0, "multi-or-nest" ],
    'brace-style': 0,
    'brace-rules/brace-on-same-line': ["error", {
      "FunctionDeclaration": "always",
      "FunctionExpression": "always",
      "ArrowFunctionExpression": "always",
      "IfStatement": "never",
      "TryStatement": "never",
      "CatchClause": "never",
      "DoWhileStatement": "never",
      "WhileStatement": "never",
      "ForStatement": "never",
      "ForInStatement": "never",
      "ForOfStatement": "never",
      "SwitchStatement": "never"
    }, { "allowSingleLine": true }],
    'generator-star-spacing': 'off',
    // allow debugger during development
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-extra-boolean-cast': 0,
    "no-mixed-spaces-and-tabs": [2],
    'no-multi-spaces': ["error", { "ignoreEOLComments": true, "exceptions": { "VariableDeclarator": true } }],
    "no-tabs": 2,
    'operator-linebreak': [2, "before"],
    "no-trailing-spaces": [2, { "skipBlankLines": true }]
  }
}
