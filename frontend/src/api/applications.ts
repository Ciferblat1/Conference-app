import type {ApplicationCreateRequest, ApplicationResponse} from '../types/api'


export async function getApplications(): Promise<ApplicationResponse[]> {

    const response = await fetch("/api/applications/me")

    if(!response.ok) {
        throw new Error(`Не удалось загрузить зявки: ${response.status}`)
    }

    return response.json();
}


export async function createApplication(data: ApplicationCreateRequest): Promise<ApplicationResponse> {
    
    const formData = new FormData();
    formData.append("directionId", data.directionId)
    formData.append("file", data.file);

    const response = await fetch("/api/applications", {
        method: "POST",
        body: formData
    });

    if(!response.ok) {
        const problem = await response.json().catch(() => null);
        throw new Error(problem?.detail ?? `Не удалось подать заявку: ${response.status}`)
    }

    return response.json();
}