export type Sensor = {
  id: number;
  name: string;
  type: string;
  latitude: number;
  longitude: number;
  active: boolean;
  createdAt: string;
};

export type ProblemDetail = {
  title: string;
  status: number;
  detail: string;
};