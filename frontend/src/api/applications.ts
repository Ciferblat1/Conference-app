import type {ApplicationResponse} from '../types/api'


export async function getApplications(): Promise<ApplicationResponse[]> {

    const response = await fetch("/api/applications/me")

    if(!response.ok) {
        throw new Error(`Не удалось загрузить зявки: ${response.status}`)
    }

    return response.json();
}