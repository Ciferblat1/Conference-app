import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router";
import { createApplication } from "../api/applications";
import { notifications } from "@mantine/notifications";
import { Alert, Anchor, Button, Container, FileInput, Select, Stack, Title } from "@mantine/core";
import { Controller, useForm } from "react-hook-form";
import { useDirections } from "../hooks/useDirections";


interface FormValues {
    directionId: string,
    file?: File
}

const MAX_FILE_SIZE = 10 * 1024 * 1024

export default function CreateApplicationPage() {

    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const {data: directions = []} = useDirections()

    const {control, handleSubmit} = useForm<FormValues>({
        defaultValues: {directionId: ''},
    })

    const mutation = useMutation({
        mutationFn: createApplication,
        onSuccess: async() => {
            await queryClient.invalidateQueries({queryKey: ['applications'], refetchType: 'all'})
            notifications.show({message: 'Заявка подана'})
            navigate('/')
        }
    })

    return(
        <Container component="main" size="sm" py="md">
            <Title>Подать заявку</Title>

            <form onSubmit={handleSubmit(values => {
                if(!values.file) return
                mutation.mutate({directionId: values.directionId, file: values.file})
            })}>

                <Stack>
                    <Controller 
                        name="directionId"
                        control={control}
                        rules={{required: 'Выберите направление'}}
                        render={({field, fieldState}) => (
                            <Select
                                label="Направление"
                                placeholder="Выберите направление"
                                data={directions.map(d => ({value: d.id, label: d.title}))}
                                value={field.value || null}
                                onChange={value => field.onChange(value ?? '')}
                                error={fieldState.error?.message}
                            />
                        )}
                    />

                    <Controller
                        name="file"
                        control={control}
                        rules={{
                            validate: file => {
                                if(!file) return 'Выберите файл'
                                if(!/\.docx?$/i.test(file.name)) return 'Допустимы только .doc и .docx'
                                if(file.size > MAX_FILE_SIZE) return 'Файл не должен превышать 10 МБ'
                                return true
                            },
                        }}
                        render={({field, fieldState}) => (
                            <FileInput
                                label="Документ (.doc или .docx)"
                                placeholder="Выберите файл"
                                accept=".doc,.docx"
                                clearable
                                value={field.value ?? null}
                                onChange={file => field.onChange(file ?? undefined)}
                                error={fieldState.error?.message}
                            />
                        )}
                    />

                    {mutation.isError && (
                        <Alert color="red">{mutation.error.message}</Alert>
                    )}

                    <Button type="submit" loading={mutation.isPending}>
                        Подать заявку
                    </Button>

                    <Anchor component={Link} to="/">Назад к моим заявкам</Anchor>
                </Stack>

            </form>
        </Container>
    )

}