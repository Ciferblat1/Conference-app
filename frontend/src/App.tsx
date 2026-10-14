import {useQuery} from '@tanstack/react-query'

import {getDirections} from './api/directions'

import './App.css'

function App() {

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

  return (
    <main>
      <h1>направления конференции</h1>

      {directions.length === 0 ? (
        <p>направлений пока нет</p>
      ): (
        <ul>
          {directions.map(direction => (
            <li key={direction.id}>{direction.title}</li>
          ))}
        </ul>
      )}
    </main>
  )
}

export default App
