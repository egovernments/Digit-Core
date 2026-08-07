import React from "react";
import { TrackChanges } from "./TrackChanges";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TrackChanges",
  component: TrackChanges,
};

export const Default = () => <TrackChanges />;
export const Fill = () => <TrackChanges fill="blue" />;
export const Size = () => <TrackChanges height="50" width="50" />;
export const CustomStyle = () => <TrackChanges style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TrackChanges className="custom-class" />;
export const Clickable = () => <TrackChanges onClick={()=>console.log("clicked")} />;

const Template = (args) => <TrackChanges {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
