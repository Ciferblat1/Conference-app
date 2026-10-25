import {useForm} from 'react-hook-form'
import {useMutation, useQueryClient} from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router'
import { Alert, Anchor, Button, Container, PasswordInput, Stack, TextInput, Title } from '@mantine/core'
import { notifications } from '@mantine/notifications'

import { login } from '../api/auth'
import type { LoginRequest } from '../types/api'


export default function LoginPage() {

    const navigate = useNavigate()

    const {register: field, handleSubmit, formState: {errors}} = useForm<LoginRequest>()

    const queryClient = useQueryClient()

    const mutation = useMutation({
        mutationFn: login,
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: ['me']})
            notifications.show({message: 'Вход прошёл успешно'})
            navigate("/")
        }
    })



    return(
        <Container size="xs" mt="xl"> 
            <Title mb="md">Вход</Title>

            <form onSubmit={handleSubmit(values => mutation.mutate(values))}>
                <Stack>
                    <TextInput 
                        label="Email"
                        error={errors.email?.message}
                        {...field('email', {
                            required: 'Введите email',
                            maxLength: {value: 255, message: "Не длиннее 255 символов"},
                            })
                        }
                    />

                    <PasswordInput 
                        label="Password"
                        error={errors.password?.message}
                        {...field('password', {
                            required: 'Введите пароль',
                            minLength: {value: 8, message: "Не менее 8 символов"},
                            maxLength: {value: 72, message: "Не длиннее 72 символов"},
                            })
                        }
                    />

                    {mutation.isError && (
                        <Alert color="red">{mutation.error.message}</Alert>
                    )}

                    <Button type="submit" loading={mutation.isPending}>
                        Войти
                    </Button>

                    <Anchor component={Link} to="/register">Еще нет аккаунта? Зарегистрироваться</Anchor>

                </Stack>

            </form>
        </Container>
    )
}