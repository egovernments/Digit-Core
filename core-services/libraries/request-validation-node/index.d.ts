/// <reference types="node" />
import { IncomingMessage, ServerResponse } from 'http';

export type Mode = 'REPORT' | 'ENFORCE';
export type Activation = 'ANNOTATED' | 'ALL';
export type ViolationCode =
  | 'REQUEST_CONTENT_NOT_ALLOWED' | 'REQUEST_JSON_MALFORMED' | 'REQUEST_LIMIT_EXCEEDED' | 'REQUEST_JSON_DUPLICATE_KEY';
export type ContentRuleId = 'R1' | 'R2' | 'R3' | 'R4';

export interface Violation {
  readonly code: ViolationCode;
  readonly ruleId: string;      // R1..R4 or a structural/limit rule id (java-constants.json rule_ids)
  readonly location: string;    // JSON-pointer-like path, "/" for the root, a bare segment for scalars, "*" when unsafe
  readonly length: number;      // UTF-16 length of the offending value, or 0
}

export interface Limits {
  maxBodyBytes: number; maxDepth: number; maxStringLength: number; maxNameLength: number;
  maxTokens: number; maxNumberLength: number; maxScalarLength: number;
}
export interface Rules {
  markupStart: boolean; urlScheme: boolean; eventHandler: boolean; disallowedControls: number[];
  deniedSchemes: string[]; deniedDataMediaTypes: string[]; decodeRounds: 0 | 1 | 2 | 3; normalizeNfkc: boolean;
}

/** The @ValidateRequest equivalent. undefined always means "inherit". */
export interface RoutePolicy {
  path: string;                  // raw (undecoded) pathname; exact match unless prefix is true
  prefix?: boolean;              // match path and everything below it, at a segment boundary
  method?: string;               // e.g. "POST"; omitted = any method
  name?: string;                 // handler label for logs; default "<METHOD or *> <path>"
  enabled?: boolean;
  structured?: boolean;
  mode?: Mode;
  skipPaths?: string[];          // unioned with every enclosing scope
  limits?: Partial<Limits>;      // each field inherits independently
  reason?: string;               // required for enabled:false, structured:false or skipPaths (startup WARN otherwise)
}
export interface ExcludePath { path: string; reason?: string; }

export interface Logger { warn(line: string): void; info?(line: string): void; error?(line: string): void; }

export interface Options {
  enabled?: boolean;
  structuredDefault?: boolean;
  activation?: Activation;
  mode?: Mode;
  inspectContentTypeHeader?: boolean;
  rejectDuplicateKeys?: boolean;
  rejectDualRequestInfo?: boolean;
  log?: { reportSampleRate?: number };
  limits?: Partial<Limits>;
  rules?: Partial<Rules>;
  routes?: RoutePolicy[];
  excludePaths?: Array<string | ExcludePath>;      // non-handler surfaces (reverse proxies); never inspected
  maxInspectionMillis?: number;                     // Node-only, default unset (off)
  slowInspectionWarnMillis?: number;                // Node-only, default 250, 0 = off, log only
  logger?: Logger;                                  // default: console.warn / console.info
  logContext?: (req: IncomingMessage) => { [key: string]: string | number | boolean | null | undefined } | undefined;
  respond?: (req: IncomingMessage, res: ServerResponse,
             error: { status: 400 | 500; code: string; message: string }) => void;   // default: exact tracer bytes
  env?: { [name: string]: string | undefined } | false;                            // default: process.env
}

export type Next = (err?: any) => void;
export type Middleware = (req: IncomingMessage, res: ServerResponse, next: Next) => void;
export type ErrorMiddleware = (err: any, req: IncomingMessage, res: ServerResponse, next: Next) => void;
export type VerifyHook = (req: IncomingMessage, res: ServerResponse, buf: Buffer, encoding: string) => void;

export interface InspectJsonOptions {
  limits?: Partial<Limits>; rules?: Partial<Rules>; skipPaths?: string[];
  rejectDuplicateKeys?: boolean; rejectDualRequestInfo?: boolean;
}
export interface InspectJsonResult { findings: Violation[]; failure: Violation | null; }

export interface EffectiveConfig {
  readonly enabled: boolean; readonly structuredDefault: boolean | null; readonly activation: Activation;
  readonly mode: Mode; readonly inspectContentTypeHeader: boolean; readonly rejectDuplicateKeys: boolean;
  readonly rejectDualRequestInfo: boolean; readonly reportSampleRate: number;
  readonly limits: Readonly<Limits>; readonly rules: Readonly<Rules>;
  readonly routes: ReadonlyArray<Readonly<RoutePolicy>>; readonly excludePaths: ReadonlyArray<Readonly<ExcludePath>>;
  readonly maxInspectionMillis: number | null; readonly slowInspectionWarnMillis: number;
}

export interface RequestValidation {
  readonly enabled: boolean;
  readonly config: Readonly<EffectiveConfig>;
  readonly beforeParsers: Middleware;
  readonly jsonVerify: VerifyHook;
  readonly formVerify: VerifyHook;
  readonly afterParsers: [Middleware, ErrorMiddleware];
  readonly pathParams: Middleware;
  readonly multipartFields: Middleware;            // after a multipart parser (multer, busboy): its text fields
  inspectJsonBuffer(req: IncomingMessage, body: Buffer): void;
  detect(value: string): ContentRuleId | null;
  inspectJson(body: Buffer, options?: InspectJsonOptions): InspectJsonResult;
}

export declare class RequestValidationError extends Error {
  constructor(code?: ViolationCode | 'INTERNAL_SERVER_ERROR');   // default REQUEST_CONTENT_NOT_ALLOWED
  readonly status: 400 | 500;
  readonly statusCode: 400 | 500;
  readonly expose: boolean;
  readonly code: ViolationCode | 'INTERNAL_SERVER_ERROR';
}
export declare function isRequestValidationError(err: unknown): err is RequestValidationError;
export declare function createRequestValidation(options?: Options): RequestValidation;
export declare const VIOLATION_MESSAGES: { readonly [K in ViolationCode]: string };
export declare const CONFORMS_TO: {
  readonly jarSha256: string; readonly jsoup: string; readonly jacksonCore: string; readonly javaCharacterData: string;
};

// The core classes, exported for tests and tools.
export declare const ViolationCode: {
  readonly REQUEST_CONTENT_NOT_ALLOWED: 'REQUEST_CONTENT_NOT_ALLOWED';
  readonly REQUEST_JSON_MALFORMED: 'REQUEST_JSON_MALFORMED';
  readonly REQUEST_LIMIT_EXCEEDED: 'REQUEST_LIMIT_EXCEEDED';
  readonly REQUEST_JSON_DUPLICATE_KEY: 'REQUEST_JSON_DUPLICATE_KEY';
  readonly VALUES: ReadonlyArray<ViolationCode>;
  readonly VIOLATION_MESSAGES: { readonly [K in ViolationCode]: string };
  isViolationCode(value: unknown): value is ViolationCode;
  getMessage(code: ViolationCode): string;
};
export declare class Violation {
  constructor(code: ViolationCode, ruleId: string, location: string, length: number);
  toString(): string;            // "code|ruleId|location|length"
}
export declare class InspectionError extends Error {
  constructor(violation: Violation);
  readonly violation: Violation;
}
export declare class InspectionLimits implements Limits {
  constructor(maxBodyBytes: number, maxDepth: number, maxStringLength: number, maxNameLength: number,
              maxTokens: number, maxNumberLength: number, maxScalarLength: number);
  readonly maxBodyBytes: number; readonly maxDepth: number; readonly maxStringLength: number;
  readonly maxNameLength: number; readonly maxTokens: number; readonly maxNumberLength: number;
  readonly maxScalarLength: number;
  static defaults(): InspectionLimits;
  static fromObject(values: Partial<Limits>, base?: InspectionLimits | null): InspectionLimits;
  toJSON(): Limits;
}
export declare class ContentPolicy implements Rules {
  constructor(options?: Partial<Rules>);
  readonly markupStart: boolean; readonly urlScheme: boolean; readonly eventHandler: boolean;
  readonly disallowedControls: number[]; readonly deniedSchemes: string[]; readonly deniedDataMediaTypes: string[];
  readonly decodeRounds: 0 | 1 | 2 | 3; readonly normalizeNfkc: boolean;
  static defaults(): ContentPolicy;
  toJSON(): Rules;
}
export declare class ContentDetector {
  constructor(policy: ContentPolicy | Partial<Rules>);
  readonly policy: ContentPolicy;
  detect(value: string): ContentRuleId | null;
}
export declare class SkipPathMatcher {
  constructor(pointers: string[]);
  matches(path: string[]): boolean;
  readonly isEmpty: boolean;
}
export declare const SafeLocationFormatter: {
  isSafeSegment(value: unknown): boolean;
  scalar(value: string): string;
  format(segments: string[]): string;
  sanitizeLocation(value: string | null | undefined): string;
};
export declare class JsonDocumentInspector {
  constructor(detector: ContentDetector);
  inspect(body: Buffer | Uint8Array, limits: InspectionLimits, skipPaths: SkipPathMatcher, rejectDuplicateKeys: boolean,
          rejectDualRequestInfo: boolean, contentViolations: (violation: Violation) => void,
          options?: { deadline?: number; now?: () => number }): void;
}
