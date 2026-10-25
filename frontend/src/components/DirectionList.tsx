import { useDirections } from '../hooks/useDirections';

export default function DirectionList() {

  const {data: directions = [], isPending, isError, error} = useDirections()

  if(isPending) {
    return <p>Загрузка направлений</p>
  }

  if(isError) {
    return <p role="alert">{error.message}</p>
  }

  return(
    <ul>
        {directions.map(direction => (
            <li key={direction.id}>{direction.title}</li>
        ))}
    </ul>
  )
}

