import React from "react";
import { Terrain } from "./Terrain";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Terrain",
  component: Terrain,
};

export const Default = () => <Terrain />;
export const Fill = () => <Terrain fill="blue" />;
export const Size = () => <Terrain height="50" width="50" />;
export const CustomStyle = () => <Terrain style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Terrain className="custom-class" />;
export const Clickable = () => <Terrain onClick={()=>console.log("clicked")} />;

const Template = (args) => <Terrain {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
