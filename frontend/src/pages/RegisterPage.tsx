import {useForm} from 'react-hook-form'
import {useMutation} from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router'
import { Alert, Anchor, Button, Container, PasswordInput, Stack, TextInput, Title } from '@mantine/core'
import { notifications } from '@mantine/notifications'

import { register } from '../api/auth'
import type { RegisterRequest } from '../types/api'


export default function RegisterPage() {

    const navigate = useNavigate()

    const {register: field, handleSubmit, formState: {errors}} = useForm<RegisterRequest>()

    const mutation = useMutation({
        mutationFn: register,
        onSuccess: () => {
            notifications.show({message: 'Регистрация прошла успешно'})
            navigate("/login")
        }
    })



    return(
        <Container size="xs" mt="xl"> 
            <Title mb="md">Регистрация</Title>

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

                    <TextInput 
                        label="Fullname"
                        error={errors.fullname?.message}
                        {...field('fullname', {
                            required: 'Введите ФИО',
                            validate: value => value.trim() !== '' || 'Введите ФИО',
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
                        Зарегистрироваться
                    </Button>

                    <Anchor component={Link} to="/login">Уже есть аккаунт? Войти</Anchor>

                </Stack>

            </form>
        </Container>
    )
}