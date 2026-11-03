export type ProblemDetail = {
    title: string;
    status: number;
    detail: string;
    errors?: Record<string, string>;
};