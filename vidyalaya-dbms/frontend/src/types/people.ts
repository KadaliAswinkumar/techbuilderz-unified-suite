export type StudentDto = {
  id: string;
  fullName: string;
  firstName?: string | null;
  middleName?: string | null;
  lastName?: string | null;
  email?: string | null;
  phone?: string | null;
  gender?: string | null;
  fatherName?: string | null;
  motherName?: string | null;
  fatherOccupation?: string | null;
  motherOccupation?: string | null;
  dateOfBirth?: string | null;
  religion?: string | null;
  caste?: string | null;
  address?: string | null;
  className?: string | null;
  section?: string | null;
  admissionDate?: string | null;
  photoUrl?: string | null;
  socialLinks?: string | null;
  aboutStudent?: string | null;
};

export type TeacherDto = {
  id: string;
  fullName: string;
  email?: string | null;
  phone?: string | null;
  gender?: string | null;
  dateOfBirth?: string | null;
  address?: string | null;
  qualification?: string | null;
  experienceSummary?: string | null;
  joiningDate?: string | null;
  salaryAmount?: number | null;
  photoUrl?: string | null;
  socialLinks?: string | null;
};

export type ParentDto = {
  id: string;
  fullName: string;
  email?: string | null;
  phone?: string | null;
  address?: string | null;
  occupation?: string | null;
  employer?: string | null;
  educationSummary?: string | null;
  photoUrl?: string | null;
  socialLinks?: string | null;
  childrenCount?: number;
};
