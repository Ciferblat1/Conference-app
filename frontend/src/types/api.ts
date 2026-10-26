export interface DirectionResponse {
    id: string,
    title: string
}


export type UserRole = 'PARTICIPANT' | 'ORGANIZER' | 'EXPERT'



export interface LoginRequest {
    email: string,
    password: string
}



export interface RegisterRequest {
    email: string,
    fullname: string,
    password: string
}



export interface UserResponse {
    id: string,
    email: string,
    fullname: string,
    role: UserRole
}


export interface ApplicationResponse {
    id: string, 
    authorId: string, 
    directionId: string, 
    status: ApplicationStatus, 
    createdAt: string, 
    updatedAt: string
}

export interface ApplicationCreateRequest {
    directionId: string,
    file: File
}


export type ApplicationStatus = 'SUBMITTED' | 'APPROVED' | 'WITHDRAWN'