import { Stack, Text } from '@mantine/core';
import { useDirections } from '../hooks/useDirections';

export default function DirectionList() {

  const {data: directions = [], isPending, isError, error} = useDirections()

  if(isPending) {
    return <p>Загрузка направлений</p>
  }

  if(isError) {
    return <p role="alert">{error.message}</p>
  }

  if(directions.length === 0) {
    return <Text ta="center" c="dimmed">Направлений пока нет</Text>
  }

  return(
    <Stack align="center" gap="xs">
      {directions.map(direction => (
        <Text key={direction.id}>{direction.title}</Text>
      ))}
    </Stack>
  )
}

