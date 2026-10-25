import {useQuery} from '@tanstack/react-query'

import {getDirections} from '../api/directions'

export default function DirectionList() {

    const {data: directions = [], isPending, isError, error} = useQuery({
    queryKey: ['directions'],
    queryFn: getDirections,
  })

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

