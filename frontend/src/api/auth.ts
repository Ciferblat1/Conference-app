import type {UserResponse, RegisterRequest, LoginRequest} from '../types/api'

//Регистрация
export async function register(data: RegisterRequest): Promise<UserResponse> {
    
    const response = await fetch("/api/auth/register", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(data),
    });

    if(!response.ok) {
        const problem = await response.json().catch(() => null);
        throw new Error(problem?.detail ?? `Не удалось зарегистрироваться: ${response.status}`)
    }

    return response.json();
}

//Вход
export async function login(data: LoginRequest): Promise<void> {
    
    //Бэкенд принимает форму, а не JSON
    const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
        body: new URLSearchParams({email: data.email, password: data.password}),
    });


    if(response.status === 401) {
        throw new Error("Неверный email или пароль")
    }

    if(!response.ok) {
        throw new Error(`Не удалось войти: ${response.status}`)
    }
}


//Выход
export async function logout(): Promise<void> {
    const response = await fetch("/api/auth/logout", {
        method: "POST"
    });

    if(!response.ok) {
        throw new Error(`Не удалось выйти: ${response.status}`)
    }
}


export async function getMe(): Promise<UserResponse | null> {
    const response = await fetch("/api/auth/me");

    if(response.status === 401) {
        return null
    }

    if(!response.ok) {
        throw new Error(`Не удалось получить пользователя: ${response.status}`)
    }

    return response.json();
}