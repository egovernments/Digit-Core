import React from "react";
import { TheaterComedy } from "./TheaterComedy";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TheaterComedy",
  component: TheaterComedy,
};

export const Default = () => <TheaterComedy />;
export const Fill = () => <TheaterComedy fill="blue" />;
export const Size = () => <TheaterComedy height="50" width="50" />;
export const CustomStyle = () => <TheaterComedy style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TheaterComedy className="custom-class" />;
export const Clickable = () => <TheaterComedy onClick={()=>console.log("clicked")} />;

const Template = (args) => <TheaterComedy {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
