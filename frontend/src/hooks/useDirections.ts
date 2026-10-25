import { useQuery } from "@tanstack/react-query";
import { getDirections } from "../api/directions";


export function useDirections() {
    return useQuery({
        queryKey: ['directions'],
        queryFn: getDirections,
    })
}