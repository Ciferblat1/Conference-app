import type {DirectionResponse} from '../types/api'

export async function getDirections(): Promise<DirectionResponse[]> {
    
    const response = await fetch("/api/directions");

    if(!response.ok) {
        throw new Error(`Не удалось загрузить направления: ${response.status}`)
    }

    return response.json();
}