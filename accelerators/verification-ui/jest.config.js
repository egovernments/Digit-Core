/** @type {import('ts-jest').JestConfigWithTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  rootDir: './',
  testPathIgnorePatterns: ["<rootDir>/node_modules"],
  testMatch: ["<rootDir>/src/**/*.test.ts"],
  setupFilesAfterEnv: ["<rootDir>/src/setupTests.ts"]
};
