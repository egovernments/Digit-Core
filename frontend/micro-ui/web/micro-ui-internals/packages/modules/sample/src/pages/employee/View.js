import React from 'react'

const View = () => {
  const ViewEstimate = Digit.ComponentRegistryService.getComponent("ViewEstimatePage");
  return (
    <ViewEstimate />
  )
}

export default View